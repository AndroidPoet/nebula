package io.github.androidpoet.nebula.protocol.operations

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.*

/** Protocol header with version and API level. */
public class HeaderOperation(
  public val version: Int,
  public val apiLevel: Int,
  public val width: Float = 0f,
  public val height: Float = 0f,
) : Operation(Operations.HEADER) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.HEADER)
    buffer.writeInt(version)
    buffer.writeInt(apiLevel)
    buffer.writeFloat(width)
    buffer.writeFloat(height)
  }

  public companion object {
    public fun read(buffer: WireBuffer): HeaderOperation {
      return HeaderOperation(
        version = buffer.readInt(),
        apiLevel = buffer.readInt(),
        width = buffer.readFloat(),
        height = buffer.readFloat(),
      )
    }
  }
}

/** Animation spec defines easing and duration for animated values. */
public class AnimationSpecOperation(
  public val targetId: Int,
  public val easingType: Int,
  public val duration: Float,
) : Operation(Operations.ANIMATION_SPEC) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.ANIMATION_SPEC)
    buffer.writeInt(targetId)
    buffer.writeInt(easingType)
    buffer.writeFloat(duration)
  }

  public companion object {
    public fun read(buffer: WireBuffer): AnimationSpecOperation {
      return AnimationSpecOperation(
        targetId = buffer.readInt(),
        easingType = buffer.readInt(),
        duration = buffer.readFloat(),
      )
    }
  }
}

/** Theme definition with color mappings. */
public class ThemeOperation(
  public val themeType: Int,
  public val colors: Map<Int, Long>,
) : Operation(Operations.THEME) {

  override fun applyData(context: RemoteContext) {
    for ((id, color) in colors) {
      context.loadColor(id, color)
    }
  }

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.THEME)
    buffer.writeInt(themeType)
    buffer.writeInt(colors.size)
    for ((id, color) in colors) {
      buffer.writeInt(id)
      buffer.writeColor(color)
    }
  }

  public companion object {
    public fun read(buffer: WireBuffer): ThemeOperation {
      val type = buffer.readInt()
      val count = buffer.readInt()
      val colors = mutableMapOf<Int, Long>()
      repeat(count) {
        val id = buffer.readInt()
        val color = buffer.readColor()
        colors[id] = color
      }
      return ThemeOperation(type, colors)
    }
  }
}

/** Color theme reference. */
public class ColorThemeOperation(
  public val id: Int,
  public val lightColor: Long,
  public val darkColor: Long,
) : Operation(Operations.COLOR_THEME) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.COLOR_THEME)
    buffer.writeInt(id)
    buffer.writeColor(lightColor)
    buffer.writeColor(darkColor)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ColorThemeOperation {
      return ColorThemeOperation(
        id = buffer.readInt(),
        lightColor = buffer.readColor(),
        darkColor = buffer.readColor(),
      )
    }
  }
}

/** Debug message for development inspection. */
public class DebugMessageOperation(
  public val message: String,
) : Operation(Operations.DEBUG_MESSAGE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DEBUG_MESSAGE)
    buffer.writeString(message)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DebugMessageOperation {
      return DebugMessageOperation(buffer.readString())
    }
  }
}

/** Haptic feedback request. */
public class HapticFeedbackOperation(
  public val feedbackType: Int,
) : Operation(Operations.HAPTIC_FEEDBACK) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.HAPTIC_FEEDBACK)
    buffer.writeInt(feedbackType)
  }

  public companion object {
    public fun read(buffer: WireBuffer): HapticFeedbackOperation {
      return HapticFeedbackOperation(buffer.readInt())
    }
  }
}

/** Accessibility semantics for a component. */
public class AccessibilitySemanticsOperation(
  public val contentDescription: String,
  public val role: Int,
  public val stateDescription: String,
) : Operation(Operations.ACCESSIBILITY_SEMANTICS) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.ACCESSIBILITY_SEMANTICS)
    buffer.writeString(contentDescription)
    buffer.writeInt(role)
    buffer.writeString(stateDescription)
  }

  public companion object {
    public fun read(buffer: WireBuffer): AccessibilitySemanticsOperation {
      return AccessibilitySemanticsOperation(
        contentDescription = buffer.readString(),
        role = buffer.readInt(),
        stateDescription = buffer.readString(),
      )
    }
  }
}
