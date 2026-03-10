package io.github.androidpoet.nebula.protocol.operations

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.*

/** Width modifier. Values: WRAP = -1, FILL = -2, exact dp = positive. */
public class ModifierWidthOperation(
  public val width: Float,
) : Operation(Operations.MODIFIER_WIDTH) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_WIDTH)
    buffer.writeFloat(width)
  }

  public companion object {
    public const val WRAP: Float = -1f
    public const val FILL: Float = -2f

    public fun read(buffer: WireBuffer): ModifierWidthOperation {
      return ModifierWidthOperation(width = buffer.readFloat())
    }
  }
}

/** Height modifier. */
public class ModifierHeightOperation(
  public val height: Float,
) : Operation(Operations.MODIFIER_HEIGHT) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_HEIGHT)
    buffer.writeFloat(height)
  }

  public companion object {
    public const val WRAP: Float = -1f
    public const val FILL: Float = -2f

    public fun read(buffer: WireBuffer): ModifierHeightOperation {
      return ModifierHeightOperation(height = buffer.readFloat())
    }
  }
}

/** Width range modifier (min/max). */
public class ModifierWidthInOperation(
  public val minWidth: Float,
  public val maxWidth: Float,
) : Operation(Operations.MODIFIER_WIDTH_IN) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_WIDTH_IN)
    buffer.writeFloat(minWidth)
    buffer.writeFloat(maxWidth)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierWidthInOperation {
      return ModifierWidthInOperation(
        minWidth = buffer.readFloat(),
        maxWidth = buffer.readFloat(),
      )
    }
  }
}

/** Height range modifier (min/max). */
public class ModifierHeightInOperation(
  public val minHeight: Float,
  public val maxHeight: Float,
) : Operation(Operations.MODIFIER_HEIGHT_IN) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_HEIGHT_IN)
    buffer.writeFloat(minHeight)
    buffer.writeFloat(maxHeight)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierHeightInOperation {
      return ModifierHeightInOperation(
        minHeight = buffer.readFloat(),
        maxHeight = buffer.readFloat(),
      )
    }
  }
}

/** Padding modifier with individual edge values. */
public class ModifierPaddingOperation(
  public val start: Float,
  public val top: Float,
  public val end: Float,
  public val bottom: Float,
) : Operation(Operations.MODIFIER_PADDING) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_PADDING)
    buffer.writeFloat(start)
    buffer.writeFloat(top)
    buffer.writeFloat(end)
    buffer.writeFloat(bottom)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierPaddingOperation {
      return ModifierPaddingOperation(
        start = buffer.readFloat(), top = buffer.readFloat(),
        end = buffer.readFloat(), bottom = buffer.readFloat(),
      )
    }
  }
}

/** Background modifier with color and corner radius. */
public class ModifierBackgroundOperation(
  public val color: Long,
  public val cornerRadius: Float = 0f,
) : Operation(Operations.MODIFIER_BACKGROUND) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_BACKGROUND)
    buffer.writeColor(color)
    buffer.writeFloat(cornerRadius)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierBackgroundOperation {
      return ModifierBackgroundOperation(
        color = buffer.readColor(),
        cornerRadius = buffer.readFloat(),
      )
    }
  }
}

/** Border modifier. */
public class ModifierBorderOperation(
  public val width: Float,
  public val color: Long,
  public val cornerRadius: Float = 0f,
) : Operation(Operations.MODIFIER_BORDER) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_BORDER)
    buffer.writeFloat(width)
    buffer.writeColor(color)
    buffer.writeFloat(cornerRadius)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierBorderOperation {
      return ModifierBorderOperation(
        width = buffer.readFloat(),
        color = buffer.readColor(),
        cornerRadius = buffer.readFloat(),
      )
    }
  }
}

/** Rectangular clip modifier. */
public class ModifierClipRectOperation : Operation(Operations.MODIFIER_CLIP_RECT) {
  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_CLIP_RECT)
  }
}

/** Rounded rectangle clip modifier. */
public class ModifierRoundedClipOperation(
  public val topStart: Float,
  public val topEnd: Float,
  public val bottomEnd: Float,
  public val bottomStart: Float,
) : Operation(Operations.MODIFIER_ROUNDED_CLIP_RECT) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_ROUNDED_CLIP_RECT)
    buffer.writeFloat(topStart)
    buffer.writeFloat(topEnd)
    buffer.writeFloat(bottomEnd)
    buffer.writeFloat(bottomStart)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierRoundedClipOperation {
      return ModifierRoundedClipOperation(
        topStart = buffer.readFloat(), topEnd = buffer.readFloat(),
        bottomEnd = buffer.readFloat(), bottomStart = buffer.readFloat(),
      )
    }
  }
}

/** Click modifier with action ID. */
public class ModifierClickOperation(
  public val actionId: Int,
  public val contentDescription: String = "",
) : Operation(Operations.MODIFIER_CLICK) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_CLICK)
    buffer.writeInt(actionId)
    buffer.writeString(contentDescription)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierClickOperation {
      return ModifierClickOperation(
        actionId = buffer.readInt(),
        contentDescription = buffer.readString(),
      )
    }
  }
}

/** Visibility modifier. */
public class ModifierVisibilityOperation(
  public val visible: Boolean,
) : Operation(Operations.MODIFIER_VISIBILITY) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_VISIBILITY)
    buffer.writeBoolean(visible)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierVisibilityOperation {
      return ModifierVisibilityOperation(visible = buffer.readBoolean())
    }
  }
}

/** Offset modifier. */
public class ModifierOffsetOperation(
  public val x: Float,
  public val y: Float,
) : Operation(Operations.MODIFIER_OFFSET) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_OFFSET)
    buffer.writeFloat(x)
    buffer.writeFloat(y)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierOffsetOperation {
      return ModifierOffsetOperation(x = buffer.readFloat(), y = buffer.readFloat())
    }
  }
}

/** Scroll modifier. 0 = vertical, 1 = horizontal. */
public class ModifierScrollOperation(
  public val direction: Int,
) : Operation(Operations.MODIFIER_SCROLL) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_SCROLL)
    buffer.writeInt(direction)
  }

  public companion object {
    public const val VERTICAL: Int = 0
    public const val HORIZONTAL: Int = 1

    public fun read(buffer: WireBuffer): ModifierScrollOperation {
      return ModifierScrollOperation(direction = buffer.readInt())
    }
  }
}

/** Z-index modifier for paint order. */
public class ModifierZIndexOperation(
  public val zIndex: Float,
) : Operation(Operations.MODIFIER_ZINDEX) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_ZINDEX)
    buffer.writeFloat(zIndex)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierZIndexOperation {
      return ModifierZIndexOperation(zIndex = buffer.readFloat())
    }
  }
}

/** Graphics layer modifier (alpha, rotation, scale, etc.). */
public class ModifierGraphicsLayerOperation(
  public val alpha: Float = 1f,
  public val rotationX: Float = 0f,
  public val rotationY: Float = 0f,
  public val rotationZ: Float = 0f,
  public val scaleX: Float = 1f,
  public val scaleY: Float = 1f,
  public val translationX: Float = 0f,
  public val translationY: Float = 0f,
  public val shadowElevation: Float = 0f,
) : Operation(Operations.MODIFIER_GRAPHICS_LAYER) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_GRAPHICS_LAYER)
    buffer.writeFloat(alpha)
    buffer.writeFloat(rotationX)
    buffer.writeFloat(rotationY)
    buffer.writeFloat(rotationZ)
    buffer.writeFloat(scaleX)
    buffer.writeFloat(scaleY)
    buffer.writeFloat(translationX)
    buffer.writeFloat(translationY)
    buffer.writeFloat(shadowElevation)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierGraphicsLayerOperation {
      return ModifierGraphicsLayerOperation(
        alpha = buffer.readFloat(),
        rotationX = buffer.readFloat(), rotationY = buffer.readFloat(), rotationZ = buffer.readFloat(),
        scaleX = buffer.readFloat(), scaleY = buffer.readFloat(),
        translationX = buffer.readFloat(), translationY = buffer.readFloat(),
        shadowElevation = buffer.readFloat(),
      )
    }
  }
}

/** Touch down modifier. */
public class ModifierTouchDownOperation(
  public val actionId: Int,
) : Operation(Operations.MODIFIER_TOUCH_DOWN) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_TOUCH_DOWN)
    buffer.writeInt(actionId)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierTouchDownOperation {
      return ModifierTouchDownOperation(actionId = buffer.readInt())
    }
  }
}

/** Touch up modifier. */
public class ModifierTouchUpOperation(
  public val actionId: Int,
) : Operation(Operations.MODIFIER_TOUCH_UP) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MODIFIER_TOUCH_UP)
    buffer.writeInt(actionId)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ModifierTouchUpOperation {
      return ModifierTouchUpOperation(actionId = buffer.readInt())
    }
  }
}
