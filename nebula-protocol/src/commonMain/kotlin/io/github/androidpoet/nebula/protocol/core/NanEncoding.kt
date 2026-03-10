package io.github.androidpoet.nebula.protocol.core

/**
 * IEEE 754 NaN encoding for embedding integer IDs inside float values.
 *
 * The AndroidX Compose Remote protocol encodes variable references,
 * expression operators, and path commands as NaN float values. The
 * significand bits carry a 22-bit ID while the exponent bits are all
 * set (making it a NaN).
 *
 * ID space partitioning (22 usable bits):
 * - 0x0xxxxx: System variables (time, density, touch, sensors)
 * - 0x1xxxxx: User variables
 * - 0x2xxxxx: Array/collection variables
 * - 0x3xxxxx: Operations (path commands at 0x300_000, math at 0x310_000)
 */
public object NanEncoding {

  /** Convert an integer ID to a NaN-encoded float. */
  public fun asNan(id: Int): Float {
    return Float.fromBits(id or -0x800000)
  }

  /** Extract the integer ID from a NaN-encoded float. */
  public fun idFromNan(value: Float): Int {
    return value.toRawBits() and 0x3FFFFF
  }

  /** Check if a float value is a NaN-encoded ID. */
  public fun isNan(value: Float): Boolean = value.isNaN()

  /** Resolve a float value — if it's a NaN, look up the ID in the variable store. */
  public fun resolve(value: Float, variables: FloatVariableStore): Float {
    return if (value.isNaN()) {
      val id = idFromNan(value)
      variables.getFloat(id)
    } else {
      value
    }
  }
}

/** ID space constants matching AndroidX NanMap. */
public object NanMap {

  // ── System Variable IDs ────────────────────────────

  public const val SYSTEM_BASE: Int = 0x000000
  public const val ID_CONTINUOUS_SEC: Int = 0x000001
  public const val ID_DENSITY: Int = 0x000002
  public const val ID_FONT_SCALE: Int = 0x000003
  public const val ID_WINDOW_WIDTH: Int = 0x000004
  public const val ID_WINDOW_HEIGHT: Int = 0x000005
  public const val ID_ANIMATION_TIME: Int = 0x000006
  public const val ID_TOUCH_POS_X: Int = 0x000007
  public const val ID_TOUCH_POS_Y: Int = 0x000008
  public const val ID_TOUCH_DOWN: Int = 0x000009
  public const val ID_TOUCH_DRAG_X: Int = 0x00000A
  public const val ID_TOUCH_DRAG_Y: Int = 0x00000B
  public const val ID_ACCELEROMETER_X: Int = 0x00000C
  public const val ID_ACCELEROMETER_Y: Int = 0x00000D
  public const val ID_ACCELEROMETER_Z: Int = 0x00000E
  public const val ID_SCROLL_X: Int = 0x00000F
  public const val ID_SCROLL_Y: Int = 0x000010

  // ── User Variable Space ────────────────────────────

  public const val USER_BASE: Int = 0x100000

  // ── Array/Collection Variable Space ────────────────

  public const val ARRAY_BASE: Int = 0x200000

  // ── Operation Space ────────────────────────────────

  public const val OPERATION_BASE: Int = 0x300000
  public const val PATH_COMMAND_BASE: Int = 0x300000
  public const val MATH_OPERATOR_BASE: Int = 0x310000

  /** Pre-encoded NaN floats for system variables. */
  public val FLOAT_CONTINUOUS_SEC: Float = NanEncoding.asNan(ID_CONTINUOUS_SEC)
  public val FLOAT_DENSITY: Float = NanEncoding.asNan(ID_DENSITY)
  public val FLOAT_FONT_SCALE: Float = NanEncoding.asNan(ID_FONT_SCALE)
  public val FLOAT_WINDOW_WIDTH: Float = NanEncoding.asNan(ID_WINDOW_WIDTH)
  public val FLOAT_WINDOW_HEIGHT: Float = NanEncoding.asNan(ID_WINDOW_HEIGHT)
  public val FLOAT_ANIMATION_TIME: Float = NanEncoding.asNan(ID_ANIMATION_TIME)
  public val FLOAT_TOUCH_POS_X: Float = NanEncoding.asNan(ID_TOUCH_POS_X)
  public val FLOAT_TOUCH_POS_Y: Float = NanEncoding.asNan(ID_TOUCH_POS_Y)
  public val FLOAT_TOUCH_DOWN: Float = NanEncoding.asNan(ID_TOUCH_DOWN)
  public val FLOAT_ACCELEROMETER_X: Float = NanEncoding.asNan(ID_ACCELEROMETER_X)
  public val FLOAT_ACCELEROMETER_Y: Float = NanEncoding.asNan(ID_ACCELEROMETER_Y)
  public val FLOAT_ACCELEROMETER_Z: Float = NanEncoding.asNan(ID_ACCELEROMETER_Z)
}

/** Interface for resolving float variable IDs at runtime. */
public interface FloatVariableStore {
  public fun getFloat(id: Int): Float
  public fun setFloat(id: Int, value: Float)
  public fun hasFloat(id: Int): Boolean
}
