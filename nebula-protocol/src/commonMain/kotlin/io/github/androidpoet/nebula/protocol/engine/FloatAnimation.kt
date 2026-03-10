package io.github.androidpoet.nebula.protocol.engine

import kotlin.math.*

/**
 * Eased float animation between two values.
 *
 * Supports 8 easing modes matching AndroidX Compose Remote's animation system.
 * Decodes the animation spec from a float array: [easing, duration, ...].
 */
public class FloatAnimation(spec: FloatArray) {

  private val easingType: Int = if (spec.isNotEmpty()) spec[0].toInt() else 0
  private val duration: Float = if (spec.size > 1) spec[1] else 0.3f

  private var startValue: Float = 0f
  private var targetValue: Float = 0f
  private var elapsed: Float = 0f

  public var currentValue: Float = 0f
    private set

  public val isFinished: Boolean get() = elapsed >= duration

  public fun setTarget(target: Float) {
    startValue = currentValue
    targetValue = target
    elapsed = 0f
  }

  public fun update(deltaSec: Float) {
    elapsed = (elapsed + deltaSec).coerceAtMost(duration)
    val progress = if (duration > 0f) elapsed / duration else 1f
    val eased = applyEasing(progress)
    currentValue = startValue + (targetValue - startValue) * eased
  }

  private fun applyEasing(t: Float): Float = when (easingType) {
    FloatExpression.EASING_LINEAR -> t
    FloatExpression.EASING_EASE_IN -> t * t
    FloatExpression.EASING_EASE_OUT -> t * (2f - t)
    FloatExpression.EASING_EASE_IN_OUT -> if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t
    FloatExpression.EASING_OVERSHOOT -> {
      val s = 1.70158f
      (t - 1f) * (t - 1f) * ((s + 1f) * (t - 1f) + s) + 1f
    }
    FloatExpression.EASING_BOUNCE -> bounceEasing(t)
    FloatExpression.EASING_ANTICIPATE -> {
      val s = 1.70158f
      t * t * ((s + 1f) * t - s)
    }
    else -> t
  }

  private fun bounceEasing(t: Float): Float {
    val n1 = 7.5625f
    val d1 = 2.75f
    return when {
      t < 1f / d1 -> n1 * t * t
      t < 2f / d1 -> { val t2 = t - 1.5f / d1; n1 * t2 * t2 + 0.75f }
      t < 2.5f / d1 -> { val t2 = t - 2.25f / d1; n1 * t2 * t2 + 0.9375f }
      else -> { val t2 = t - 2.625f / d1; n1 * t2 * t2 + 0.984375f }
    }
  }
}
