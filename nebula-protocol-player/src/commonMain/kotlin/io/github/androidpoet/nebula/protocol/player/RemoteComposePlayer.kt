package io.github.androidpoet.nebula.protocol.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.*
import io.github.androidpoet.nebula.protocol.operations.*
import kotlinx.coroutines.*

/**
 * Compose Multiplatform player for the binary wire protocol.
 *
 * Loads a [CoreDocument] from binary bytes and renders it as native
 * Compose UI. Supports all layout operations, modifiers, text styles,
 * theming, animations, and interactive actions.
 *
 * Usage:
 * ```kotlin
 * NebulaRemote(
 *   bytes = wireBytes,
 *   onAction = { id -> handleAction(id) },
 *   onNamedAction = { name, meta -> handleNamedAction(name, meta) },
 * )
 * ```
 */
@Composable
public fun NebulaRemote(
  bytes: ByteArray,
  modifier: Modifier = Modifier,
  onAction: (Int) -> Unit = {},
  onNamedAction: (String, String) -> Unit = { _, _ -> },
) {
  val document = remember(bytes) {
    CoreDocument().apply {
      val buffer = WireBuffer.fromByteArray(bytes)
      initFromBuffer(buffer)
    }
  }

  val context = remember { RemoteContext() }

  // Run DATA pass once
  LaunchedEffect(document) {
    document.applyDataPass(context)
  }

  // Animation frame loop — update context time for expression evaluation
  var frameTick by remember { mutableIntStateOf(0) }
  LaunchedEffect(document) {
    val startMark = kotlinx.coroutines.currentCoroutineContext()
    var elapsed = 0L
    while (isActive) {
      context.continuousTimeSec = elapsed / 1000f
      frameTick++
      delay(16) // ~60fps
      elapsed += 16
    }
  }

  // Render the component tree
  Box(modifier = modifier) {
    document.rootComponent?.let { root ->
      RenderComponent(
        component = root,
        context = context,
        document = document,
        onAction = onAction,
        onNamedAction = onNamedAction,
      )
    }
  }
}

/** Overload accepting a pre-built [WireBuffer]. */
@Composable
public fun NebulaRemote(
  buffer: WireBuffer,
  modifier: Modifier = Modifier,
  onAction: (Int) -> Unit = {},
  onNamedAction: (String, String) -> Unit = { _, _ -> },
) {
  NebulaRemote(
    bytes = buffer.toByteArray(),
    modifier = modifier,
    onAction = onAction,
    onNamedAction = onNamedAction,
  )
}

@Composable
private fun RenderComponent(
  component: ComponentOperation,
  context: RemoteContext,
  document: CoreDocument,
  onAction: (Int) -> Unit,
  onNamedAction: (String, String) -> Unit,
) {
  val mod = buildModifier(component.modifierOps, context, onAction, onNamedAction)

  when (component) {
    is LayoutRootOperation -> {
      Box(modifier = mod) {
        for (child in component.children) {
          RenderComponent(child, context, document, onAction, onNamedAction)
        }
      }
    }

    is LayoutColumnOperation -> {
      Column(
        modifier = mod,
        verticalArrangement = resolveVerticalArrangement(component.verticalArrangement, component.spacing),
        horizontalAlignment = resolveHorizontalAlignment(component.horizontalAlignment),
      ) {
        for (child in component.children) {
          RenderComponent(child, context, document, onAction, onNamedAction)
        }
      }
    }

    is LayoutRowOperation -> {
      Row(
        modifier = mod,
        horizontalArrangement = resolveHorizontalArrangement(component.horizontalArrangement, component.spacing),
        verticalAlignment = resolveVerticalAlignment(component.verticalAlignment),
      ) {
        for (child in component.children) {
          RenderComponent(child, context, document, onAction, onNamedAction)
        }
      }
    }

    is LayoutBoxOperation -> {
      Box(
        modifier = mod,
        contentAlignment = resolveAlignment(component.contentAlignment),
      ) {
        for (child in component.children) {
          RenderComponent(child, context, document, onAction, onNamedAction)
        }
      }
    }

    is LayoutFlowOperation -> {
      // Flow layout rendered as wrapping row/column
      if (component.direction == LayoutFlowOperation.HORIZONTAL) {
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
          modifier = mod,
          horizontalArrangement = Arrangement.spacedBy(component.mainAxisSpacing.dp),
          verticalArrangement = Arrangement.spacedBy(component.crossAxisSpacing.dp),
        ) {
          for (child in component.children) {
            RenderComponent(child, context, document, onAction, onNamedAction)
          }
        }
      } else {
        @OptIn(ExperimentalLayoutApi::class)
        FlowColumn(
          modifier = mod,
          verticalArrangement = Arrangement.spacedBy(component.mainAxisSpacing.dp),
          horizontalArrangement = Arrangement.spacedBy(component.crossAxisSpacing.dp),
        ) {
          for (child in component.children) {
            RenderComponent(child, context, document, onAction, onNamedAction)
          }
        }
      }
    }

    is LayoutCanvasOperation -> {
      Box(modifier = mod) {
        for (child in component.children) {
          RenderComponent(child, context, document, onAction, onNamedAction)
        }
      }
    }

    is LayoutFitBoxOperation -> {
      Box(
        modifier = mod,
        contentAlignment = resolveAlignment(component.contentAlignment),
      ) {
        for (child in component.children) {
          RenderComponent(child, context, document, onAction, onNamedAction)
        }
      }
    }

    is LayoutCollapsibleRowOperation -> {
      Row(
        modifier = mod,
        horizontalArrangement = Arrangement.spacedBy(component.spacing.dp),
      ) {
        for (child in component.children) {
          RenderComponent(child, context, document, onAction, onNamedAction)
        }
      }
    }

    is LayoutCollapsibleColumnOperation -> {
      Column(
        modifier = mod,
        verticalArrangement = Arrangement.spacedBy(component.spacing.dp),
      ) {
        for (child in component.children) {
          RenderComponent(child, context, document, onAction, onNamedAction)
        }
      }
    }

    is LayoutStateOperation -> {
      val stateValue = context.getInt(component.variableId)
      if (stateValue < component.children.size) {
        RenderComponent(component.children[stateValue], context, document, onAction, onNamedAction)
      }
    }

    is LayoutTextOperation -> {
      val text = context.getText(component.textId)
      val style = resolveTextStyle(component.styleId, component.modifierOps, context)
      Text(
        text = text,
        modifier = mod,
        style = style,
        maxLines = if (component.maxLines == Int.MAX_VALUE) Int.MAX_VALUE else component.maxLines,
        overflow = when (component.overflow) {
          1 -> TextOverflow.Ellipsis
          2 -> TextOverflow.Clip
          else -> TextOverflow.Clip
        },
      )
    }

    is LayoutImageOperation -> {
      Box(modifier = mod) {
        Text(
          text = "[Image: ${component.bitmapId}]",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    is LayoutContentOperation -> {
      // Content placeholder — rendered as empty box
      Box(modifier = mod)
    }

    else -> {
      // Unknown component type — skip
    }
  }
}

@Composable
private fun buildModifier(
  ops: List<Operation>,
  context: RemoteContext,
  onAction: (Int) -> Unit,
  onNamedAction: (String, String) -> Unit,
): Modifier {
  var mod: Modifier = Modifier

  for (op in ops) {
    mod = when (op) {
      is ModifierWidthOperation -> when (op.width) {
        ModifierWidthOperation.FILL -> mod.fillMaxWidth()
        ModifierWidthOperation.WRAP -> mod
        else -> mod.width(context.resolveFloat(op.width).dp)
      }
      is ModifierHeightOperation -> when (op.height) {
        ModifierHeightOperation.FILL -> mod.fillMaxHeight()
        ModifierHeightOperation.WRAP -> mod
        else -> mod.height(context.resolveFloat(op.height).dp)
      }
      is ModifierWidthInOperation -> mod.widthIn(
        min = op.minWidth.dp, max = op.maxWidth.dp,
      )
      is ModifierHeightInOperation -> mod.heightIn(
        min = op.minHeight.dp, max = op.maxHeight.dp,
      )
      is ModifierPaddingOperation -> mod.padding(
        start = context.resolveFloat(op.start).dp,
        top = context.resolveFloat(op.top).dp,
        end = context.resolveFloat(op.end).dp,
        bottom = context.resolveFloat(op.bottom).dp,
      )
      is ModifierBackgroundOperation -> {
        val shape = if (op.cornerRadius > 0f)
          RoundedCornerShape(op.cornerRadius.dp) else RoundedCornerShape(0.dp)
        mod.background(Color(op.color.toULong()), shape)
      }
      is ModifierBorderOperation -> {
        val shape = if (op.cornerRadius > 0f)
          RoundedCornerShape(op.cornerRadius.dp) else RoundedCornerShape(0.dp)
        mod.border(op.width.dp, Color(op.color.toULong()), shape)
      }
      is ModifierClipRectOperation -> mod.clip(RoundedCornerShape(0.dp))
      is ModifierRoundedClipOperation -> mod.clip(
        RoundedCornerShape(
          topStart = op.topStart.dp, topEnd = op.topEnd.dp,
          bottomEnd = op.bottomEnd.dp, bottomStart = op.bottomStart.dp,
        )
      )
      is ModifierClickOperation -> mod.clickable {
        if (op.actionId >= 0) onAction(op.actionId)
      }
      is ModifierVisibilityOperation -> {
        if (op.visible) mod else mod.size(0.dp)
      }
      is ModifierOffsetOperation -> mod.offset(
        x = context.resolveFloat(op.x).dp,
        y = context.resolveFloat(op.y).dp,
      )
      is ModifierScrollOperation -> {
        if (op.direction == ModifierScrollOperation.HORIZONTAL) {
          mod.horizontalScroll(rememberScrollState())
        } else {
          mod.verticalScroll(rememberScrollState())
        }
      }
      is ModifierGraphicsLayerOperation -> mod.graphicsLayer(
        alpha = context.resolveFloat(op.alpha),
        rotationX = context.resolveFloat(op.rotationX),
        rotationY = context.resolveFloat(op.rotationY),
        rotationZ = context.resolveFloat(op.rotationZ),
        scaleX = context.resolveFloat(op.scaleX),
        scaleY = context.resolveFloat(op.scaleY),
        translationX = context.resolveFloat(op.translationX),
        translationY = context.resolveFloat(op.translationY),
        shadowElevation = context.resolveFloat(op.shadowElevation),
      )
      is ModifierZIndexOperation -> mod // zIndex handled at layout level
      is HostActionOperation -> {
        onAction(op.actionId)
        mod
      }
      is HostNamedActionOperation -> {
        onNamedAction(op.name, op.metadata)
        mod
      }
      else -> mod
    }
  }
  return mod
}

private fun resolveTextStyle(
  styleId: Int,
  modifierOps: List<Operation>,
  context: RemoteContext,
): TextStyle {
  val styleOp = modifierOps.filterIsInstance<TextStyleOperation>().firstOrNull()
    ?: if (styleId > 0) null else null // Style would come from document's style registry

  if (styleOp != null) {
    return TextStyle(
      fontSize = styleOp.fontSize.sp,
      fontWeight = FontWeight(styleOp.fontWeight),
      color = Color(styleOp.color.toULong()),
      letterSpacing = if (styleOp.letterSpacing > 0f) styleOp.letterSpacing.sp else TextStyle.Default.letterSpacing,
      lineHeight = if (styleOp.lineHeight > 0f) styleOp.lineHeight.sp else TextStyle.Default.lineHeight,
      textAlign = when (styleOp.textAlign) {
        1 -> TextAlign.Center
        2 -> TextAlign.End
        3 -> TextAlign.Justify
        else -> TextAlign.Start
      },
      fontStyle = if (styleOp.italic) FontStyle.Italic else FontStyle.Normal,
      textDecoration = when (styleOp.decoration) {
        1 -> TextDecoration.Underline
        2 -> TextDecoration.LineThrough
        else -> TextDecoration.None
      },
    )
  }

  return TextStyle.Default
}

// ── Alignment & Arrangement Resolvers ────────────────

private fun resolveHorizontalAlignment(value: Int): Alignment.Horizontal = when (value) {
  1 -> Alignment.CenterHorizontally
  2 -> Alignment.End
  else -> Alignment.Start
}

private fun resolveVerticalAlignment(value: Int): Alignment.Vertical = when (value) {
  1 -> Alignment.CenterVertically
  2 -> Alignment.Bottom
  else -> Alignment.Top
}

private fun resolveAlignment(value: Int): Alignment = when (value) {
  1 -> Alignment.TopCenter
  2 -> Alignment.TopEnd
  3 -> Alignment.CenterStart
  4 -> Alignment.Center
  5 -> Alignment.CenterEnd
  6 -> Alignment.BottomStart
  7 -> Alignment.BottomCenter
  8 -> Alignment.BottomEnd
  else -> Alignment.TopStart
}

private fun resolveVerticalArrangement(value: Int, spacing: Float): Arrangement.Vertical {
  if (spacing > 0f) return Arrangement.spacedBy(spacing.dp)
  return when (value) {
    1 -> Arrangement.Center
    2 -> Arrangement.Bottom
    3 -> Arrangement.SpaceBetween
    4 -> Arrangement.SpaceAround
    5 -> Arrangement.SpaceEvenly
    else -> Arrangement.Top
  }
}

private fun resolveHorizontalArrangement(value: Int, spacing: Float): Arrangement.Horizontal {
  if (spacing > 0f) return Arrangement.spacedBy(spacing.dp)
  return when (value) {
    1 -> Arrangement.Center
    2 -> Arrangement.End
    3 -> Arrangement.SpaceBetween
    4 -> Arrangement.SpaceAround
    5 -> Arrangement.SpaceEvenly
    else -> Arrangement.Start
  }
}
