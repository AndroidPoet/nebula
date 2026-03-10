package io.github.androidpoet.nebula.protocol.core

import io.github.androidpoet.nebula.protocol.WireBuffer

/**
 * Base class for all wire protocol operations.
 *
 * Each operation represents a single command in the binary stream.
 * Operations implement dual-mode execution: DATA mode for resource
 * loading and PAINT mode for rendering.
 */
public abstract class Operation(public val opcode: Int) {

  /** Apply this operation during the DATA pass. */
  public open fun applyData(context: RemoteContext) {}

  /** Apply this operation during the PAINT pass. */
  public open fun applyPaint(context: RemoteContext) {}

  /** Serialize this operation to the wire buffer. */
  public abstract fun write(buffer: WireBuffer)
}

/** Context mode for dual-pass execution. */
public enum class ContextMode {
  UNSET,
  DATA,
  PAINT,
}

/**
 * Runtime context for operation execution.
 *
 * Maintains the variable store, current mode, time state,
 * and provides abstract methods for platform-specific rendering.
 */
public open class RemoteContext : FloatVariableStore {

  public var mode: ContextMode = ContextMode.UNSET

  /** System time in seconds since document load. */
  public var continuousTimeSec: Float = 0f

  /** Current animation time in seconds. */
  public var animationTimeSec: Float = 0f

  /** Display density. */
  public var density: Float = 1f

  /** Font scale. */
  public var fontScale: Float = 1f

  /** Viewport dimensions. */
  public var windowWidth: Float = 0f
  public var windowHeight: Float = 0f

  /** Touch state. */
  public var touchX: Float = 0f
  public var touchY: Float = 0f
  public var touchDown: Float = 0f

  /** Accelerometer values. */
  public var accelerometerX: Float = 0f
  public var accelerometerY: Float = 0f
  public var accelerometerZ: Float = 0f

  // ── Float variable store ───────────────────────────

  private val floatVars = mutableMapOf<Int, Float>()
  private val colorVars = mutableMapOf<Int, Long>()
  private val textVars = mutableMapOf<Int, String>()
  private val intVars = mutableMapOf<Int, Int>()
  private val boolVars = mutableMapOf<Int, Boolean>()
  private val namedVars = mutableMapOf<String, Int>()

  override fun getFloat(id: Int): Float {
    return when {
      id == NanMap.ID_CONTINUOUS_SEC -> continuousTimeSec
      id == NanMap.ID_ANIMATION_TIME -> animationTimeSec
      id == NanMap.ID_DENSITY -> density
      id == NanMap.ID_FONT_SCALE -> fontScale
      id == NanMap.ID_WINDOW_WIDTH -> windowWidth
      id == NanMap.ID_WINDOW_HEIGHT -> windowHeight
      id == NanMap.ID_TOUCH_POS_X -> touchX
      id == NanMap.ID_TOUCH_POS_Y -> touchY
      id == NanMap.ID_TOUCH_DOWN -> touchDown
      id == NanMap.ID_ACCELEROMETER_X -> accelerometerX
      id == NanMap.ID_ACCELEROMETER_Y -> accelerometerY
      id == NanMap.ID_ACCELEROMETER_Z -> accelerometerZ
      else -> floatVars[id] ?: 0f
    }
  }

  override fun setFloat(id: Int, value: Float) {
    floatVars[id] = value
  }

  override fun hasFloat(id: Int): Boolean {
    return id in floatVars || id <= NanMap.ID_SCROLL_Y
  }

  public fun loadColor(id: Int, color: Long) {
    colorVars[id] = color
  }

  public fun getColor(id: Int): Long = colorVars[id] ?: 0xFF000000

  public fun loadText(id: Int, text: String) {
    textVars[id] = text
  }

  public fun getText(id: Int): String = textVars[id] ?: ""

  public fun loadInt(id: Int, value: Int) {
    intVars[id] = value
  }

  public fun getInt(id: Int): Int = intVars[id] ?: 0

  public fun loadBoolean(id: Int, value: Boolean) {
    boolVars[id] = value
  }

  public fun getBoolean(id: Int): Boolean = boolVars[id] ?: false

  /** Register a named variable mapping to an ID. */
  public fun registerNamedVariable(name: String, id: Int) {
    namedVars[name] = id
  }

  /** Resolve a named variable to its ID. */
  public fun resolveNamedVariable(name: String): Int? = namedVars[name]

  /** Resolve a float value, handling NaN-encoded variable references. */
  public fun resolveFloat(value: Float): Float {
    return NanEncoding.resolve(value, this)
  }

  /** Resolve a color value (may be expression-based). */
  public fun resolveColor(id: Int): Long = colorVars[id] ?: 0xFF000000
}
