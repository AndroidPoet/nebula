package io.github.androidpoet.nebula.renderer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.androidpoet.nebula.components.NebulaAction
import io.github.androidpoet.nebula.components.NebulaActionHandler
import io.github.androidpoet.nebula.components.NebulaModifier
import io.github.androidpoet.nebula.components.NebulaPadding
import io.github.androidpoet.nebula.components.NebulaShape
import io.github.androidpoet.nebula.components.ScrollDirection
import io.github.androidpoet.nebula.components.ShapeType

/**
 * Converts [NebulaModifier] → Compose [Modifier] chain.
 * Order matters: size → shadow → background → clip → border → padding → visual → interaction.
 */
internal object ModifierResolver {

  @Composable
  fun resolve(
    nebula: NebulaModifier?,
    actionHandler: NebulaActionHandler?,
    base: Modifier = Modifier,
  ): Modifier {
    if (nebula == null) return base
    var m = base

    // Size constraints
    nebula.width?.let { m = m.width(it.dp) }
    nebula.height?.let { m = m.height(it.dp) }
    nebula.minWidth?.let { m = m.requiredWidthIn(min = it.dp) }
    nebula.minHeight?.let { m = m.requiredHeightIn(min = it.dp) }
    nebula.maxWidth?.let { m = m.requiredWidthIn(max = it.dp) }
    nebula.maxHeight?.let { m = m.requiredHeightIn(max = it.dp) }
    if (nebula.fillMaxSize == true) m = m.fillMaxSize()
    if (nebula.fillMaxWidth == true) m = m.fillMaxWidth()
    if (nebula.fillMaxHeight == true) m = m.fillMaxHeight()
    nebula.aspectRatio?.let { m = m.aspectRatio(it) }

    // Shadow (before background/clip)
    nebula.shadow?.let { shadow ->
      val shape = resolveShape(shadow.shape)
      m = m.shadow(elevation = shadow.elevation.dp, shape = shape)
    }

    // Background
    val bgShape = resolveShape(nebula.shape)
    nebula.background?.let { color ->
      ColorResolver.resolve(color)?.let { m = m.background(it, bgShape) }
    }

    // Clip
    nebula.clip?.let { m = m.clip(resolveShape(it)) }

    // Border
    nebula.border?.let { border ->
      val borderShape = resolveShape(border.shape ?: nebula.shape)
      val borderColor = ColorResolver.resolveOrDefault(border.color)
      m = m.border(width = border.width.dp, color = borderColor, shape = borderShape)
    }

    // Padding
    nebula.padding?.let { m = m.then(resolvePadding(it)) }

    // Scroll
    nebula.scrollable?.let { dir ->
      val scrollState = remember { ScrollState(0) }
      m = when (dir) {
        ScrollDirection.Vertical -> m.verticalScroll(scrollState)
        ScrollDirection.Horizontal -> m.horizontalScroll(scrollState)
      }
    }

    // Visual transforms
    nebula.alpha?.let { m = m.alpha(it) }
    nebula.rotate?.let { m = m.rotate(it) }
    nebula.scale?.let { m = m.scale(it) }
    if (nebula.offsetX != null || nebula.offsetY != null) {
      m = m.offset(
        x = (nebula.offsetX ?: 0f).dp,
        y = (nebula.offsetY ?: 0f).dp,
      )
    }

    // Click
    nebula.clickAction?.let { action ->
      m = m.clickable { actionHandler?.onAction(action) }
    }

    return m
  }

  private fun resolvePadding(padding: NebulaPadding): Modifier {
    val start = (padding.start ?: padding.horizontal ?: padding.all ?: 0f).dp
    val end = (padding.end ?: padding.horizontal ?: padding.all ?: 0f).dp
    val top = (padding.top ?: padding.vertical ?: padding.all ?: 0f).dp
    val bottom = (padding.bottom ?: padding.vertical ?: padding.all ?: 0f).dp
    return Modifier.padding(start = start, end = end, top = top, bottom = bottom)
  }
}

internal fun resolveShape(shape: NebulaShape?): Shape {
  if (shape == null) return RectangleShape
  return when (shape.type) {
    ShapeType.Rectangle -> RectangleShape
    ShapeType.Circle -> CircleShape
    ShapeType.Rounded -> {
      if (shape.topStart != null || shape.topEnd != null ||
        shape.bottomStart != null || shape.bottomEnd != null
      ) {
        RoundedCornerShape(
          topStart = (shape.topStart ?: shape.cornerRadius).dp,
          topEnd = (shape.topEnd ?: shape.cornerRadius).dp,
          bottomStart = (shape.bottomStart ?: shape.cornerRadius).dp,
          bottomEnd = (shape.bottomEnd ?: shape.cornerRadius).dp,
        )
      } else {
        RoundedCornerShape(shape.cornerRadius.dp)
      }
    }
  }
}
