package io.github.androidpoet.nebula.protocol.core

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.engine.FloatExpression
import io.github.androidpoet.nebula.protocol.operations.*

/**
 * Central document model for the binary protocol.
 *
 * Manages the flat operation list, component tree, float expressions,
 * and dual-pass execution model (DATA → PAINT). The document is loaded
 * from a [WireBuffer] and rendered by the player.
 */
public class CoreDocument {

  /** Flat ordered list of all operations. */
  public val operations: MutableList<Operation> = mutableListOf()

  /** Root layout component (set during inflation). */
  public var rootComponent: ComponentOperation? = null
    private set

  /** Float expression registry for reactive evaluation. */
  public val floatExpressions: MutableMap<Int, FloatExpression> = mutableMapOf()

  /** Click area registry. */
  public val clickAreas: MutableList<ClickArea> = mutableListOf()

  /** Protocol version from the header. */
  public var version: Int = 0
    private set

  /** Document API level. */
  public var apiLevel: Int = 0
    private set

  /** Content behavior (scroll mode). */
  public var contentBehavior: ContentBehavior = ContentBehavior.NONE
    internal set

  /** Content description for accessibility. */
  public var contentDescription: String = ""
    internal set

  /** Whether a new measure pass is needed. */
  public var needsMeasure: Boolean = true

  /** Time of last update in nanoseconds. */
  private var lastTimeNanos: Long = 0L

  /**
   * Initialize the document from a wire buffer.
   *
   * Deserializes all operations, then inflates the component tree
   * by matching container start/end pairs.
   */
  public fun initFromBuffer(buffer: WireBuffer) {
    operations.clear()
    buffer.rewind()

    while (buffer.remaining > 0) {
      val opcode = buffer.readOpcode()
      val operation = deserializeOperation(opcode, buffer)
      if (operation != null) {
        operations.add(operation)
      }
    }

    inflateComponents()
  }

  /**
   * Execute the DATA pass — loads resources, registers variables,
   * evaluates data expressions.
   */
  public fun applyDataPass(context: RemoteContext) {
    context.mode = ContextMode.DATA
    for (op in operations) {
      op.applyData(context)
    }
  }

  /**
   * Execute the PAINT pass — renders the document.
   *
   * Updates time variables, evaluates dirty expressions,
   * runs layout if needed, then paints all operations.
   */
  public fun applyPaintPass(context: RemoteContext, currentTimeNanos: Long) {
    val deltaSec = if (lastTimeNanos == 0L) 0f
    else (currentTimeNanos - lastTimeNanos) / 1_000_000_000f
    lastTimeNanos = currentTimeNanos

    // Update system time variables
    context.continuousTimeSec += deltaSec
    context.animationTimeSec += deltaSec

    // Evaluate dirty float expressions
    var hasRunningAnimations = false
    for ((_, expr) in floatExpressions) {
      val result = expr.evaluate(context)
      context.setFloat(expr.id, result)
      if (expr.updateAnimation(deltaSec)) {
        hasRunningAnimations = true
      }
    }

    // Paint pass
    context.mode = ContextMode.PAINT
    for (op in operations) {
      op.applyPaint(context)
    }

    if (hasRunningAnimations) {
      needsMeasure = true
    }
  }

  /** Handle a click event at the given coordinates. */
  public fun onClick(x: Float, y: Float): Int? {
    for (area in clickAreas) {
      if (x in area.left..area.right && y in area.top..area.bottom) {
        return area.actionId
      }
    }
    return null
  }

  /**
   * Convert the flat operation list into a nested component tree.
   *
   * Containers (Column, Row, Box, etc.) are matched with their
   * corresponding CONTAINER_END to form parent-child relationships.
   */
  private fun inflateComponents() {
    val stack = ArrayDeque<ComponentOperation>()

    for (op in operations) {
      when (op) {
        is ComponentOperation -> {
          if (stack.isNotEmpty()) {
            stack.last().children.add(op)
          }
          if (op.isContainer) {
            stack.addLast(op)
          }
          if (rootComponent == null && op is LayoutRootOperation) {
            rootComponent = op
          }
        }
        is ContainerEndOperation -> {
          if (stack.isNotEmpty()) {
            stack.removeLast()
          }
        }
        else -> {
          if (stack.isNotEmpty()) {
            stack.last().modifierOps.add(op)
          }
        }
      }
    }
  }

  /** Deserialize a single operation from the buffer. */
  private fun deserializeOperation(opcode: Int, buffer: WireBuffer): Operation? {
    return when (opcode) {
      Operations.HEADER -> HeaderOperation.read(buffer).also {
        version = it.version
        apiLevel = it.apiLevel
      }
      Operations.CONTAINER_END -> ContainerEndOperation()
      Operations.ROOT_CONTENT_BEHAVIOR -> {
        val behavior = buffer.readByte()
        contentBehavior = ContentBehavior.fromCode(behavior)
        null
      }
      Operations.ROOT_CONTENT_DESCRIPTION -> {
        contentDescription = buffer.readString()
        null
      }

      // Data operations
      Operations.DATA_FLOAT -> DataFloatOperation.read(buffer)
      Operations.ANIMATED_FLOAT -> AnimatedFloatOperation.read(buffer).also {
        floatExpressions[it.id] = it.toFloatExpression()
      }
      Operations.DATA_TEXT -> DataTextOperation.read(buffer)
      Operations.DATA_INT -> DataIntOperation.read(buffer)
      Operations.DATA_BOOLEAN -> DataBooleanOperation.read(buffer)
      Operations.DATA_LONG -> DataLongOperation.read(buffer)
      Operations.COLOR_CONSTANT -> ColorConstantOperation.read(buffer)
      Operations.NAMED_VARIABLE -> NamedVariableOperation.read(buffer)
      Operations.FLOAT_LIST -> FloatListOperation.read(buffer)

      // Paint
      Operations.PAINT_VALUES -> PaintValuesOperation.read(buffer)

      // Draw operations
      Operations.DRAW_RECT -> DrawRectOperation.read(buffer)
      Operations.DRAW_ROUND_RECT -> DrawRoundRectOperation.read(buffer)
      Operations.DRAW_CIRCLE -> DrawCircleOperation.read(buffer)
      Operations.DRAW_OVAL -> DrawOvalOperation.read(buffer)
      Operations.DRAW_LINE -> DrawLineOperation.read(buffer)
      Operations.DRAW_ARC -> DrawArcOperation.read(buffer)
      Operations.DRAW_PATH -> DrawPathOperation.read(buffer)
      Operations.DRAW_BITMAP -> DrawBitmapOperation.read(buffer)
      Operations.DRAW_TEXT_RUN -> DrawTextRunOperation.read(buffer)
      Operations.DRAW_CONTENT -> DrawContentOperation()
      Operations.DRAW_TEXT_ANCHOR -> DrawTextAnchorOperation.read(buffer)

      // Matrix / Transform
      Operations.MATRIX_SAVE -> MatrixSaveOperation()
      Operations.MATRIX_RESTORE -> MatrixRestoreOperation()
      Operations.MATRIX_TRANSLATE -> MatrixTranslateOperation.read(buffer)
      Operations.MATRIX_ROTATE -> MatrixRotateOperation.read(buffer)
      Operations.MATRIX_SCALE -> MatrixScaleOperation.read(buffer)
      Operations.MATRIX_SKEW -> MatrixSkewOperation.read(buffer)

      // Clip
      Operations.CLIP_RECT -> ClipRectOperation.read(buffer)
      Operations.CLIP_PATH -> ClipPathOperation.read(buffer)

      // Layout
      Operations.LAYOUT_ROOT -> LayoutRootOperation.read(buffer)
      Operations.LAYOUT_BOX -> LayoutBoxOperation.read(buffer)
      Operations.LAYOUT_ROW -> LayoutRowOperation.read(buffer)
      Operations.LAYOUT_COLUMN -> LayoutColumnOperation.read(buffer)
      Operations.LAYOUT_TEXT -> LayoutTextOperation.read(buffer)
      Operations.LAYOUT_IMAGE -> LayoutImageOperation.read(buffer)
      Operations.LAYOUT_CANVAS -> LayoutCanvasOperation.read(buffer)
      Operations.LAYOUT_FLOW -> LayoutFlowOperation.read(buffer)
      Operations.LAYOUT_STATE -> LayoutStateOperation.read(buffer)
      Operations.LAYOUT_CONTENT -> LayoutContentOperation()
      Operations.LAYOUT_COLLAPSIBLE_ROW -> LayoutCollapsibleRowOperation.read(buffer)
      Operations.LAYOUT_COLLAPSIBLE_COLUMN -> LayoutCollapsibleColumnOperation.read(buffer)
      Operations.LAYOUT_FIT_BOX -> LayoutFitBoxOperation.read(buffer)

      // Text
      Operations.TEXT_STYLE -> TextStyleOperation.read(buffer)
      Operations.CORE_TEXT -> CoreTextOperation.read(buffer)
      Operations.TEXT_FROM_FLOAT -> TextFromFloatOperation.read(buffer)
      Operations.TEXT_MERGE -> TextMergeOperation.read(buffer)

      // Modifiers
      Operations.MODIFIER_WIDTH -> ModifierWidthOperation.read(buffer)
      Operations.MODIFIER_HEIGHT -> ModifierHeightOperation.read(buffer)
      Operations.MODIFIER_WIDTH_IN -> ModifierWidthInOperation.read(buffer)
      Operations.MODIFIER_HEIGHT_IN -> ModifierHeightInOperation.read(buffer)
      Operations.MODIFIER_PADDING -> ModifierPaddingOperation.read(buffer)
      Operations.MODIFIER_BACKGROUND -> ModifierBackgroundOperation.read(buffer)
      Operations.MODIFIER_BORDER -> ModifierBorderOperation.read(buffer)
      Operations.MODIFIER_CLIP_RECT -> ModifierClipRectOperation()
      Operations.MODIFIER_ROUNDED_CLIP_RECT -> ModifierRoundedClipOperation.read(buffer)
      Operations.MODIFIER_CLICK -> ModifierClickOperation.read(buffer)
      Operations.MODIFIER_VISIBILITY -> ModifierVisibilityOperation.read(buffer)
      Operations.MODIFIER_OFFSET -> ModifierOffsetOperation.read(buffer)
      Operations.MODIFIER_SCROLL -> ModifierScrollOperation.read(buffer)
      Operations.MODIFIER_ZINDEX -> ModifierZIndexOperation.read(buffer)
      Operations.MODIFIER_GRAPHICS_LAYER -> ModifierGraphicsLayerOperation.read(buffer)

      // Actions
      Operations.HOST_ACTION -> HostActionOperation.read(buffer)
      Operations.HOST_NAMED_ACTION -> HostNamedActionOperation.read(buffer)
      Operations.VALUE_INTEGER_CHANGE_ACTION -> ValueIntegerChangeActionOperation.read(buffer)
      Operations.VALUE_FLOAT_CHANGE_ACTION -> ValueFloatChangeActionOperation.read(buffer)
      Operations.RUN_ACTION -> RunActionOperation.read(buffer)

      // Control flow
      Operations.CONDITIONAL_OPERATIONS -> ConditionalOperation.read(buffer)
      Operations.LOOP_START -> LoopStartOperation.read(buffer)
      Operations.SKIP -> SkipOperation.read(buffer)

      // Touch
      Operations.TOUCH_EXPRESSION -> TouchExpressionOperation.read(buffer)
      Operations.MODIFIER_TOUCH_DOWN -> ModifierTouchDownOperation.read(buffer)
      Operations.MODIFIER_TOUCH_UP -> ModifierTouchUpOperation.read(buffer)

      // Expression
      Operations.INTEGER_EXPRESSION -> IntegerExpressionOperation.read(buffer)
      Operations.COLOR_EXPRESSIONS -> ColorExpressionOperation.read(buffer)

      // Canvas
      Operations.CANVAS_OPERATIONS -> CanvasOperationsOp.read(buffer)

      // Theme
      Operations.THEME -> ThemeOperation.read(buffer)
      Operations.COLOR_THEME -> ColorThemeOperation.read(buffer)

      // Particles
      Operations.PARTICLE_DEFINE -> ParticleDefineOperation.read(buffer)

      // Haptic
      Operations.HAPTIC_FEEDBACK -> HapticFeedbackOperation.read(buffer)

      // Accessibility
      Operations.ACCESSIBILITY_SEMANTICS -> AccessibilitySemanticsOperation.read(buffer)

      // Debug
      Operations.DEBUG_MESSAGE -> DebugMessageOperation.read(buffer)

      // Animation spec
      Operations.ANIMATION_SPEC -> AnimationSpecOperation.read(buffer)

      else -> {
        // Unknown opcode — try to skip if size-prefixed
        null
      }
    }
  }
}

/** Content behavior modes for the document root. */
public enum class ContentBehavior(public val code: Int) {
  NONE(0),
  SCROLL_VERTICAL(1),
  SCROLL_HORIZONTAL(2);

  public companion object {
    public fun fromCode(code: Int): ContentBehavior = entries.find { it.code == code } ?: NONE
  }
}

/** Click area with bounds and action ID. */
public data class ClickArea(
  val left: Float,
  val top: Float,
  val right: Float,
  val bottom: Float,
  val actionId: Int,
  val contentDescription: String = "",
  val metadata: String = "",
)
