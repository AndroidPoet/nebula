package io.github.androidpoet.nebula.components

import kotlinx.serialization.Serializable

/**
 * Unified modifier model. Maps directly to Compose Modifier chains.
 * All measurements in dp, all colors as hex strings.
 */
@Serializable
public data class NebulaModifier(
  // Size
  val width: Float? = null,
  val height: Float? = null,
  val minWidth: Float? = null,
  val minHeight: Float? = null,
  val maxWidth: Float? = null,
  val maxHeight: Float? = null,
  val fillMaxWidth: Boolean? = null,
  val fillMaxHeight: Boolean? = null,
  val fillMaxSize: Boolean? = null,
  val aspectRatio: Float? = null,
  val weight: Float? = null,

  // Spacing
  val padding: NebulaPadding? = null,

  // Background
  val background: String? = null,
  val shape: NebulaShape? = null,

  // Border
  val border: NebulaBorder? = null,

  // Shadow
  val shadow: NebulaShadow? = null,

  // Scroll
  val scrollable: ScrollDirection? = null,

  // Visual
  val alpha: Float? = null,
  val clip: NebulaShape? = null,
  val rotate: Float? = null,
  val scale: Float? = null,
  val offsetX: Float? = null,
  val offsetY: Float? = null,

  // Interaction
  val clickAction: NebulaAction? = null,
)

@Serializable
public data class NebulaPadding(
  val all: Float? = null,
  val horizontal: Float? = null,
  val vertical: Float? = null,
  val start: Float? = null,
  val end: Float? = null,
  val top: Float? = null,
  val bottom: Float? = null,
)

@Serializable
public data class NebulaShape(
  val type: ShapeType = ShapeType.Rounded,
  val cornerRadius: Float = 0f,
  val topStart: Float? = null,
  val topEnd: Float? = null,
  val bottomStart: Float? = null,
  val bottomEnd: Float? = null,
)

@Serializable
public data class NebulaBorder(
  val width: Float = 1f,
  val color: String = "#000000",
  val shape: NebulaShape? = null,
)

@Serializable
public data class NebulaShadow(
  val elevation: Float = 4f,
  val shape: NebulaShape? = null,
)

@Serializable
public enum class ScrollDirection {
  @kotlinx.serialization.SerialName("vertical") Vertical,
  @kotlinx.serialization.SerialName("horizontal") Horizontal,
}
