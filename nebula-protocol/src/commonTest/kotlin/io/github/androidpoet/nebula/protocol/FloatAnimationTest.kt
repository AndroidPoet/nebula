package io.github.androidpoet.nebula.protocol

import io.github.androidpoet.nebula.protocol.engine.FloatAnimation
import io.github.androidpoet.nebula.protocol.engine.FloatExpression
import io.github.androidpoet.nebula.protocol.engine.SpringStopEngine
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FloatAnimationTest {

  // ── FloatAnimation ─────────────────────────────────

  @Test
  fun test_animation_linear_startsAtStart() {
    val anim = FloatAnimation(floatArrayOf(FloatExpression.EASING_LINEAR.toFloat(), 1f))
    anim.setTarget(10f)
    assertEquals(0f, anim.currentValue)
  }

  @Test
  fun test_animation_linear_reachesTarget() {
    val anim = FloatAnimation(floatArrayOf(FloatExpression.EASING_LINEAR.toFloat(), 1f))
    anim.setTarget(10f)

    // Simulate slightly more than 1 second to account for float precision
    repeat(65) { anim.update(1f / 60f) }

    assertApprox(10f, anim.currentValue, 1f)
    assertTrue(anim.isFinished)
  }

  @Test
  fun test_animation_linear_midpoint() {
    val anim = FloatAnimation(floatArrayOf(FloatExpression.EASING_LINEAR.toFloat(), 1f))
    anim.setTarget(10f)

    anim.update(0.5f) // half the duration

    assertApprox(5f, anim.currentValue, 0.1f)
    assertFalse(anim.isFinished)
  }

  @Test
  fun test_animation_easeIn_slowerAtStart() {
    val linear = FloatAnimation(floatArrayOf(FloatExpression.EASING_LINEAR.toFloat(), 1f))
    val easeIn = FloatAnimation(floatArrayOf(FloatExpression.EASING_EASE_IN.toFloat(), 1f))

    linear.setTarget(10f)
    easeIn.setTarget(10f)

    linear.update(0.25f)
    easeIn.update(0.25f)

    // Ease-in should be behind linear at the start
    assertTrue(easeIn.currentValue < linear.currentValue)
  }

  @Test
  fun test_animation_retarget_startsFromCurrentValue() {
    val anim = FloatAnimation(floatArrayOf(FloatExpression.EASING_LINEAR.toFloat(), 1f))
    anim.setTarget(10f)
    anim.update(0.5f) // halfway → ~5

    val midValue = anim.currentValue
    anim.setTarget(20f)

    // After retarget, should start from the current value
    assertEquals(midValue, anim.currentValue)
  }

  // ── SpringStopEngine ───────────────────────────────

  @Test
  fun test_spring_startsUnfinished() {
    val spring = SpringStopEngine()
    spring.setTarget(10f)
    assertFalse(spring.isFinished)
  }

  @Test
  fun test_spring_convergesToTarget() {
    val spring = SpringStopEngine(stiffness = 500f, damping = 30f)
    spring.setTarget(10f)

    repeat(300) { spring.update(1f / 60f) }

    assertApprox(10f, spring.currentValue, 0.1f)
    assertTrue(spring.isFinished)
  }

  @Test
  fun test_spring_initiallyAccelerates() {
    val spring = SpringStopEngine(stiffness = 200f, damping = 20f)
    spring.setTarget(100f)

    spring.update(1f / 60f)
    val v1 = spring.currentValue
    spring.update(1f / 60f)
    val v2 = spring.currentValue

    // Second step should have more displacement than first (accelerating)
    assertTrue(v2 - v1 >= v1, "Spring should accelerate initially")
  }

  private fun assertApprox(expected: Float, actual: Float, tolerance: Float = 0.001f) {
    assertTrue(
      abs(expected - actual) < tolerance,
      "Expected $expected but got $actual (tolerance $tolerance)",
    )
  }
}
