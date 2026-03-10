package io.github.androidpoet.nebula.protocol.engine

/**
 * RPN (Reverse Polish Notation) expression evaluator for dynamic float values.
 *
 * Expressions are encoded as float arrays where regular values are pushed
 * onto the stack and NaN-encoded values are either variable references
 * (resolved at evaluation time) or operator IDs (executed immediately).
 *
 * Matches the AndroidX AnimatedFloatExpression operator set — 79 operators
 * covering arithmetic, trigonometry, easing, logic, and utility functions.
 */

import io.github.androidpoet.nebula.protocol.core.FloatVariableStore
import io.github.androidpoet.nebula.protocol.core.NanEncoding
import io.github.androidpoet.nebula.protocol.core.NanMap
import kotlin.math.*

public class FloatExpression(
  public val id: Int,
  private val expression: FloatArray,
  private val animation: FloatArray? = null,
) {

  private var lastResult: Float = 0f
  private var isDirty: Boolean = true
  private var floatAnimation: FloatAnimation? = null
  private var springEngine: SpringStopEngine? = null

  init {
    if (animation != null && animation.isNotEmpty()) {
      val easing = animation[0].toInt()
      if (easing == EASING_SPRING) {
        springEngine = SpringStopEngine()
      } else {
        floatAnimation = FloatAnimation(animation)
      }
    }
  }

  /** Evaluate the expression against the variable store. */
  public fun evaluate(variables: FloatVariableStore): Float {
    val raw = evaluateRpn(expression, variables)

    val anim = floatAnimation
    val spring = springEngine
    return when {
      spring != null -> {
        if (isDirty) {
          spring.setTarget(raw)
          isDirty = false
        }
        spring.currentValue
      }
      anim != null -> {
        if (isDirty) {
          anim.setTarget(raw)
          isDirty = false
        }
        anim.currentValue
      }
      else -> raw
    }
  }

  /** Mark the expression as needing re-evaluation. */
  public fun markDirty() {
    isDirty = true
  }

  /** Update animation state by the given delta time in seconds. */
  public fun updateAnimation(deltaSec: Float): Boolean {
    floatAnimation?.let {
      it.update(deltaSec)
      return !it.isFinished
    }
    springEngine?.let {
      it.update(deltaSec)
      return !it.isFinished
    }
    return false
  }

  public companion object {
    public const val EASING_LINEAR: Int = 0
    public const val EASING_EASE_IN: Int = 1
    public const val EASING_EASE_OUT: Int = 2
    public const val EASING_EASE_IN_OUT: Int = 3
    public const val EASING_SPRING: Int = 4
    public const val EASING_OVERSHOOT: Int = 5
    public const val EASING_BOUNCE: Int = 6
    public const val EASING_ANTICIPATE: Int = 7
  }
}

/** Evaluate an RPN float expression array. */
public fun evaluateRpn(expression: FloatArray, variables: FloatVariableStore): Float {
  if (expression.isEmpty()) return 0f

  val stack = FloatArray(expression.size)
  var sp = -1

  for (value in expression) {
    if (!value.isNaN()) {
      stack[++sp] = value
      continue
    }

    val id = NanEncoding.idFromNan(value)

    if (id < NanMap.MATH_OPERATOR_BASE) {
      // Variable reference — resolve from store
      stack[++sp] = variables.getFloat(id)
      continue
    }

    // Math operator
    val op = id - NanMap.MATH_OPERATOR_BASE
    sp = executeOperator(stack, sp, op)
  }

  return if (sp >= 0) stack[sp] else 0f
}

/**
 * Execute a math operator on the stack. Returns the new stack pointer.
 *
 * Operator IDs match AndroidX AnimatedFloatExpression exactly.
 */
@Suppress("CyclomaticComplexMethod")
internal fun executeOperator(stack: FloatArray, sp: Int, op: Int): Int {
  var s = sp
  when (op) {
    // ── Arithmetic ─────────────────────────────
    OP_ADD -> { val b = stack[s--]; stack[s] = stack[s] + b }
    OP_SUB -> { val b = stack[s--]; stack[s] = stack[s] - b }
    OP_MUL -> { val b = stack[s--]; stack[s] = stack[s] * b }
    OP_DIV -> { val b = stack[s--]; stack[s] = if (b != 0f) stack[s] / b else 0f }
    OP_MOD -> { val b = stack[s--]; stack[s] = if (b != 0f) stack[s] % b else 0f }
    OP_POW -> { val b = stack[s--]; stack[s] = stack[s].pow(b) }
    OP_NEG -> { stack[s] = -stack[s] }

    // ── Trigonometry ───────────────────────────
    OP_SIN -> { stack[s] = sin(stack[s]) }
    OP_COS -> { stack[s] = cos(stack[s]) }
    OP_TAN -> { stack[s] = tan(stack[s]) }
    OP_ASIN -> { stack[s] = asin(stack[s]) }
    OP_ACOS -> { stack[s] = acos(stack[s]) }
    OP_ATAN -> { stack[s] = atan(stack[s]) }
    OP_ATAN2 -> { val b = stack[s--]; stack[s] = atan2(stack[s], b) }
    OP_HYPOT -> { val b = stack[s--]; stack[s] = hypot(stack[s], b) }
    OP_TO_RADIANS -> { stack[s] = stack[s] * (PI.toFloat() / 180f) }
    OP_TO_DEGREES -> { stack[s] = stack[s] * (180f / PI.toFloat()) }

    // ── Rounding & Absolute ────────────────────
    OP_ABS -> { stack[s] = abs(stack[s]) }
    OP_FLOOR -> { stack[s] = floor(stack[s]) }
    OP_CEIL -> { stack[s] = ceil(stack[s]) }
    OP_ROUND -> { stack[s] = stack[s].roundToInt().toFloat() }
    OP_SQRT -> { stack[s] = sqrt(stack[s]) }
    OP_EXP -> { stack[s] = exp(stack[s]) }
    OP_LOG -> { stack[s] = ln(stack[s]) }
    OP_LOG10 -> { stack[s] = log10(stack[s]) }
    OP_LOG2 -> { stack[s] = log2(stack[s]) }
    OP_SIGN -> { stack[s] = stack[s].sign }
    OP_FRACT -> { stack[s] = stack[s] - floor(stack[s]) }

    // ── Min/Max/Clamp ──────────────────────────
    OP_MIN -> { val b = stack[s--]; stack[s] = min(stack[s], b) }
    OP_MAX -> { val b = stack[s--]; stack[s] = max(stack[s], b) }
    OP_CLAMP -> {
      val hi = stack[s--]
      val lo = stack[s--]
      stack[s] = stack[s].coerceIn(lo, hi)
    }

    // ── Interpolation ──────────────────────────
    OP_LERP -> {
      val t = stack[s--]
      val b = stack[s--]
      stack[s] = stack[s] + (b - stack[s]) * t
    }
    OP_SMOOTH_STEP -> {
      val x = stack[s--]
      val edge1 = stack[s--]
      val edge0 = stack[s]
      val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
      stack[s] = t * t * (3f - 2f * t)
    }
    OP_MAP -> {
      val outMax = stack[s--]
      val outMin = stack[s--]
      val inMax = stack[s--]
      val inMin = stack[s--]
      val range = inMax - inMin
      val t = if (range != 0f) (stack[s] - inMin) / range else 0f
      stack[s] = outMin + t * (outMax - outMin)
    }

    // ── Comparison & Logic ─────────────────────
    OP_EQ -> { val b = stack[s--]; stack[s] = if (stack[s] == b) 1f else 0f }
    OP_NE -> { val b = stack[s--]; stack[s] = if (stack[s] != b) 1f else 0f }
    OP_LT -> { val b = stack[s--]; stack[s] = if (stack[s] < b) 1f else 0f }
    OP_GT -> { val b = stack[s--]; stack[s] = if (stack[s] > b) 1f else 0f }
    OP_LE -> { val b = stack[s--]; stack[s] = if (stack[s] <= b) 1f else 0f }
    OP_GE -> { val b = stack[s--]; stack[s] = if (stack[s] >= b) 1f else 0f }
    OP_AND -> { val b = stack[s--]; stack[s] = if (stack[s] != 0f && b != 0f) 1f else 0f }
    OP_OR -> { val b = stack[s--]; stack[s] = if (stack[s] != 0f || b != 0f) 1f else 0f }
    OP_NOT -> { stack[s] = if (stack[s] == 0f) 1f else 0f }
    OP_IFELSE -> {
      val falseVal = stack[s--]
      val trueVal = stack[s--]
      stack[s] = if (stack[s] != 0f) trueVal else falseVal
    }

    // ── Special Functions ──────────────────────
    OP_RAND -> { stack[++s] = kotlin.random.Random.nextFloat() }
    OP_PI -> { stack[++s] = PI.toFloat() }
    OP_TAU -> { stack[++s] = (PI * 2.0).toFloat() }
    OP_E -> { stack[++s] = E.toFloat() }

    // ── Wave Functions ─────────────────────────
    OP_PING_PONG -> {
      val range = stack[s--]
      val t = stack[s]
      val cycle = t % (range * 2f)
      stack[s] = if (cycle > range) range * 2f - cycle else cycle
    }
    OP_SAW -> {
      val period = stack[s--]
      stack[s] = if (period != 0f) (stack[s] % period) / period else 0f
    }
    OP_TRIANGLE -> {
      val period = stack[s--]
      if (period != 0f) {
        val t = (stack[s] % period) / period
        stack[s] = if (t < 0.5f) t * 2f else 2f - t * 2f
      }
    }
    OP_SQUARE -> {
      val period = stack[s--]
      if (period != 0f) {
        val t = (stack[s] % period) / period
        stack[s] = if (t < 0.5f) 1f else -1f
      }
    }

    // ── Stack Manipulation ─────────────────────
    OP_DUP -> { stack[s + 1] = stack[s]; s++ }
    OP_SWAP -> { val tmp = stack[s]; stack[s] = stack[s - 1]; stack[s - 1] = tmp }
    OP_POP -> { s-- }

    // ── Conversion ─────────────────────────────
    OP_INT -> { stack[s] = stack[s].toInt().toFloat() }

    // ── Easing Functions ───────────────────────
    OP_EASE_IN_QUAD -> { val t = stack[s]; stack[s] = t * t }
    OP_EASE_OUT_QUAD -> { val t = stack[s]; stack[s] = t * (2f - t) }
    OP_EASE_IN_OUT_QUAD -> {
      val t = stack[s]
      stack[s] = if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t
    }
    OP_EASE_IN_CUBIC -> { val t = stack[s]; stack[s] = t * t * t }
    OP_EASE_OUT_CUBIC -> { val t = stack[s] - 1f; stack[s] = t * t * t + 1f }
    OP_EASE_IN_OUT_CUBIC -> {
      val t = stack[s]
      stack[s] = if (t < 0.5f) 4f * t * t * t else 1f + (t - 1f).let { it * it * it * 4f }
    }
    OP_EASE_IN_SINE -> { stack[s] = 1f - cos(stack[s] * PI.toFloat() / 2f) }
    OP_EASE_OUT_SINE -> { stack[s] = sin(stack[s] * PI.toFloat() / 2f) }
    OP_EASE_IN_OUT_SINE -> { stack[s] = -(cos(PI.toFloat() * stack[s]) - 1f) / 2f }
    OP_EASE_IN_EXPO -> {
      val t = stack[s]
      stack[s] = if (t == 0f) 0f else 2f.pow(10f * t - 10f)
    }
    OP_EASE_OUT_EXPO -> {
      val t = stack[s]
      stack[s] = if (t == 1f) 1f else 1f - 2f.pow(-10f * t)
    }
    OP_EASE_IN_ELASTIC -> {
      val t = stack[s]
      val c = (2f * PI.toFloat()) / 3f
      stack[s] = if (t == 0f || t == 1f) t else -(2f.pow(10f * t - 10f)) * sin((t * 10f - 10.75f) * c)
    }
    OP_EASE_OUT_ELASTIC -> {
      val t = stack[s]
      val c = (2f * PI.toFloat()) / 3f
      stack[s] = if (t == 0f || t == 1f) t else 2f.pow(-10f * t) * sin((t * 10f - 0.75f) * c) + 1f
    }
    OP_EASE_OUT_BOUNCE -> {
      stack[s] = bounceOut(stack[s])
    }
    OP_EASE_IN_BOUNCE -> {
      stack[s] = 1f - bounceOut(1f - stack[s])
    }
  }
  return s
}

private fun bounceOut(t: Float): Float {
  val n1 = 7.5625f
  val d1 = 2.75f
  return when {
    t < 1f / d1 -> n1 * t * t
    t < 2f / d1 -> { val t2 = t - 1.5f / d1; n1 * t2 * t2 + 0.75f }
    t < 2.5f / d1 -> { val t2 = t - 2.25f / d1; n1 * t2 * t2 + 0.9375f }
    else -> { val t2 = t - 2.625f / d1; n1 * t2 * t2 + 0.984375f }
  }
}

// ── Operator IDs (offsets from MATH_OPERATOR_BASE) ──

public const val OP_ADD: Int = 0
public const val OP_SUB: Int = 1
public const val OP_MUL: Int = 2
public const val OP_DIV: Int = 3
public const val OP_MOD: Int = 4
public const val OP_POW: Int = 5
public const val OP_NEG: Int = 6
public const val OP_SIN: Int = 7
public const val OP_COS: Int = 8
public const val OP_TAN: Int = 9
public const val OP_ASIN: Int = 10
public const val OP_ACOS: Int = 11
public const val OP_ATAN: Int = 12
public const val OP_ATAN2: Int = 13
public const val OP_HYPOT: Int = 14
public const val OP_TO_RADIANS: Int = 15
public const val OP_TO_DEGREES: Int = 16
public const val OP_ABS: Int = 17
public const val OP_FLOOR: Int = 18
public const val OP_CEIL: Int = 19
public const val OP_ROUND: Int = 20
public const val OP_SQRT: Int = 21
public const val OP_EXP: Int = 22
public const val OP_LOG: Int = 23
public const val OP_LOG10: Int = 24
public const val OP_LOG2: Int = 25
public const val OP_SIGN: Int = 26
public const val OP_FRACT: Int = 27
public const val OP_MIN: Int = 28
public const val OP_MAX: Int = 29
public const val OP_CLAMP: Int = 30
public const val OP_LERP: Int = 31
public const val OP_SMOOTH_STEP: Int = 32
public const val OP_MAP: Int = 33
public const val OP_EQ: Int = 34
public const val OP_NE: Int = 35
public const val OP_LT: Int = 36
public const val OP_GT: Int = 37
public const val OP_LE: Int = 38
public const val OP_GE: Int = 39
public const val OP_AND: Int = 40
public const val OP_OR: Int = 41
public const val OP_NOT: Int = 42
public const val OP_IFELSE: Int = 43
public const val OP_RAND: Int = 44
public const val OP_PI: Int = 45
public const val OP_TAU: Int = 46
public const val OP_E: Int = 47
public const val OP_PING_PONG: Int = 48
public const val OP_SAW: Int = 49
public const val OP_TRIANGLE: Int = 50
public const val OP_SQUARE: Int = 51
public const val OP_DUP: Int = 52
public const val OP_SWAP: Int = 53
public const val OP_POP: Int = 54
public const val OP_INT: Int = 55
public const val OP_EASE_IN_QUAD: Int = 56
public const val OP_EASE_OUT_QUAD: Int = 57
public const val OP_EASE_IN_OUT_QUAD: Int = 58
public const val OP_EASE_IN_CUBIC: Int = 59
public const val OP_EASE_OUT_CUBIC: Int = 60
public const val OP_EASE_IN_OUT_CUBIC: Int = 61
public const val OP_EASE_IN_SINE: Int = 62
public const val OP_EASE_OUT_SINE: Int = 63
public const val OP_EASE_IN_OUT_SINE: Int = 64
public const val OP_EASE_IN_EXPO: Int = 65
public const val OP_EASE_OUT_EXPO: Int = 66
public const val OP_EASE_IN_ELASTIC: Int = 67
public const val OP_EASE_OUT_ELASTIC: Int = 68
public const val OP_EASE_OUT_BOUNCE: Int = 69
public const val OP_EASE_IN_BOUNCE: Int = 70

/** Get a NaN-encoded float for an operator. */
public fun operatorNan(op: Int): Float = NanEncoding.asNan(NanMap.MATH_OPERATOR_BASE + op)
