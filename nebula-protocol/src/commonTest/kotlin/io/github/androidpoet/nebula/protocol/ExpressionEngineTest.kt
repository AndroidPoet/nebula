package io.github.androidpoet.nebula.protocol

import io.github.androidpoet.nebula.protocol.core.NanMap
import io.github.androidpoet.nebula.protocol.engine.*
import kotlin.math.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpressionEngineTest {

  private val store = TestFloatStore()

  private fun eval(vararg values: Float): Float = evaluateRpn(floatArrayOf(*values), store)

  // ── Constant pushing ───────────────────────────────

  @Test
  fun test_eval_emptyExpression_returnsZero() {
    assertEquals(0f, eval())
  }

  @Test
  fun test_eval_singleConstant_returnsThatValue() {
    assertEquals(42f, eval(42f))
  }

  // ── Arithmetic ─────────────────────────────────────

  @Test
  fun test_add_twoValues() {
    assertEquals(7f, eval(3f, 4f, operatorNan(OP_ADD)))
  }

  @Test
  fun test_subtract_twoValues() {
    assertEquals(6f, eval(10f, 4f, operatorNan(OP_SUB)))
  }

  @Test
  fun test_multiply_twoValues() {
    assertEquals(12f, eval(3f, 4f, operatorNan(OP_MUL)))
  }

  @Test
  fun test_divide_twoValues() {
    assertEquals(2.5f, eval(5f, 2f, operatorNan(OP_DIV)))
  }

  @Test
  fun test_divide_byZero_returnsZero() {
    assertEquals(0f, eval(5f, 0f, operatorNan(OP_DIV)))
  }

  @Test
  fun test_modulo_twoValues() {
    assertEquals(1f, eval(7f, 3f, operatorNan(OP_MOD)))
  }

  @Test
  fun test_power_twoValues() {
    assertEquals(8f, eval(2f, 3f, operatorNan(OP_POW)))
  }

  @Test
  fun test_negate_value() {
    assertEquals(-5f, eval(5f, operatorNan(OP_NEG)))
  }

  // ── Trigonometry ───────────────────────────────────

  @Test
  fun test_sin_zero() {
    assertApprox(0f, eval(0f, operatorNan(OP_SIN)))
  }

  @Test
  fun test_cos_zero() {
    assertApprox(1f, eval(0f, operatorNan(OP_COS)))
  }

  @Test
  fun test_sin_piOverTwo() {
    assertApprox(1f, eval(PI.toFloat() / 2f, operatorNan(OP_SIN)))
  }

  @Test
  fun test_toRadians_180_isPi() {
    assertApprox(PI.toFloat(), eval(180f, operatorNan(OP_TO_RADIANS)))
  }

  @Test
  fun test_toDegrees_pi_is180() {
    assertApprox(180f, eval(PI.toFloat(), operatorNan(OP_TO_DEGREES)))
  }

  // ── Rounding & Absolute ────────────────────────────

  @Test
  fun test_abs_negativeValue() {
    assertEquals(5f, eval(-5f, operatorNan(OP_ABS)))
  }

  @Test
  fun test_floor_roundsDown() {
    assertEquals(3f, eval(3.7f, operatorNan(OP_FLOOR)))
  }

  @Test
  fun test_ceil_roundsUp() {
    assertEquals(4f, eval(3.1f, operatorNan(OP_CEIL)))
  }

  @Test
  fun test_round_nearestInt() {
    assertEquals(4f, eval(3.6f, operatorNan(OP_ROUND)))
    assertEquals(3f, eval(3.4f, operatorNan(OP_ROUND)))
  }

  @Test
  fun test_sqrt_positiveValue() {
    assertEquals(3f, eval(9f, operatorNan(OP_SQRT)))
  }

  @Test
  fun test_sign_positive() {
    assertEquals(1f, eval(42f, operatorNan(OP_SIGN)))
  }

  @Test
  fun test_sign_negative() {
    assertEquals(-1f, eval(-42f, operatorNan(OP_SIGN)))
  }

  @Test
  fun test_fract_extractsDecimal() {
    assertApprox(0.75f, eval(3.75f, operatorNan(OP_FRACT)))
  }

  // ── Min/Max/Clamp ──────────────────────────────────

  @Test
  fun test_min_returnsSmallerValue() {
    assertEquals(3f, eval(3f, 7f, operatorNan(OP_MIN)))
  }

  @Test
  fun test_max_returnsLargerValue() {
    assertEquals(7f, eval(3f, 7f, operatorNan(OP_MAX)))
  }

  @Test
  fun test_clamp_withinRange() {
    assertEquals(5f, eval(5f, 0f, 10f, operatorNan(OP_CLAMP)))
  }

  @Test
  fun test_clamp_belowMin_clampsToMin() {
    assertEquals(0f, eval(-5f, 0f, 10f, operatorNan(OP_CLAMP)))
  }

  @Test
  fun test_clamp_aboveMax_clampsToMax() {
    assertEquals(10f, eval(15f, 0f, 10f, operatorNan(OP_CLAMP)))
  }

  // ── Interpolation ──────────────────────────────────

  @Test
  fun test_lerp_atZero_returnsStart() {
    assertEquals(0f, eval(0f, 10f, 0f, operatorNan(OP_LERP)))
  }

  @Test
  fun test_lerp_atOne_returnsEnd() {
    assertEquals(10f, eval(0f, 10f, 1f, operatorNan(OP_LERP)))
  }

  @Test
  fun test_lerp_atHalf_returnsMidpoint() {
    assertEquals(5f, eval(0f, 10f, 0.5f, operatorNan(OP_LERP)))
  }

  // ── Comparison & Logic ─────────────────────────────

  @Test
  fun test_eq_equal_returnsOne() {
    assertEquals(1f, eval(5f, 5f, operatorNan(OP_EQ)))
  }

  @Test
  fun test_eq_notEqual_returnsZero() {
    assertEquals(0f, eval(5f, 3f, operatorNan(OP_EQ)))
  }

  @Test
  fun test_lt_lessThan_returnsOne() {
    assertEquals(1f, eval(3f, 5f, operatorNan(OP_LT)))
  }

  @Test
  fun test_gt_greaterThan_returnsOne() {
    assertEquals(1f, eval(5f, 3f, operatorNan(OP_GT)))
  }

  @Test
  fun test_and_bothTrue_returnsOne() {
    assertEquals(1f, eval(1f, 1f, operatorNan(OP_AND)))
  }

  @Test
  fun test_and_oneFalse_returnsZero() {
    assertEquals(0f, eval(1f, 0f, operatorNan(OP_AND)))
  }

  @Test
  fun test_or_oneTrue_returnsOne() {
    assertEquals(1f, eval(0f, 1f, operatorNan(OP_OR)))
  }

  @Test
  fun test_not_true_returnsFalse() {
    assertEquals(0f, eval(1f, operatorNan(OP_NOT)))
  }

  @Test
  fun test_not_false_returnsTrue() {
    assertEquals(1f, eval(0f, operatorNan(OP_NOT)))
  }

  @Test
  fun test_ifelse_trueCondition_returnsTrueValue() {
    // stack: condition, trueVal, falseVal → result
    assertEquals(10f, eval(1f, 10f, 20f, operatorNan(OP_IFELSE)))
  }

  @Test
  fun test_ifelse_falseCondition_returnsFalseValue() {
    assertEquals(20f, eval(0f, 10f, 20f, operatorNan(OP_IFELSE)))
  }

  // ── Constants ──────────────────────────────────────

  @Test
  fun test_pi_constant() {
    assertApprox(PI.toFloat(), eval(operatorNan(OP_PI)))
  }

  @Test
  fun test_e_constant() {
    assertApprox(E.toFloat(), eval(operatorNan(OP_E)))
  }

  // ── Stack Manipulation ─────────────────────────────

  @Test
  fun test_dup_duplicatesTop() {
    // 5, DUP → 5, 5 → ADD → 10
    assertEquals(10f, eval(5f, operatorNan(OP_DUP), operatorNan(OP_ADD)))
  }

  @Test
  fun test_swap_swapsTopTwo() {
    // 3, 7, SWAP → 7, 3 → SUB → 4
    assertEquals(4f, eval(3f, 7f, operatorNan(OP_SWAP), operatorNan(OP_SUB)))
  }

  // ── Variable Resolution ────────────────────────────

  @Test
  fun test_variableReference_resolvesFromStore() {
    store.setFloat(NanMap.USER_BASE + 1, 42f)
    val varNan = io.github.androidpoet.nebula.protocol.core.NanEncoding.asNan(NanMap.USER_BASE + 1)
    val result = evaluateRpn(floatArrayOf(varNan), store)
    assertEquals(42f, result)
  }

  @Test
  fun test_complexExpression_variableTimesConstant() {
    store.setFloat(NanMap.USER_BASE + 5, 3f)
    val varNan = io.github.androidpoet.nebula.protocol.core.NanEncoding.asNan(NanMap.USER_BASE + 5)
    val result = evaluateRpn(
      floatArrayOf(varNan, 10f, operatorNan(OP_MUL)),
      store,
    )
    assertEquals(30f, result)
  }

  // ── Complex Expressions ────────────────────────────

  @Test
  fun test_chainedArithmetic_followsRpnOrder() {
    // (3 + 4) * 2 = 14 → encoded as: 3 4 ADD 2 MUL
    assertEquals(
      14f,
      eval(3f, 4f, operatorNan(OP_ADD), 2f, operatorNan(OP_MUL)),
    )
  }

  @Test
  fun test_nestedExpression_quadraticFormula() {
    // x=3: x² + 2x + 1 = 16
    // Encoded: 3 DUP MUL 3 2 MUL ADD 1 ADD
    assertEquals(
      16f,
      eval(
        3f, operatorNan(OP_DUP), operatorNan(OP_MUL),
        3f, 2f, operatorNan(OP_MUL), operatorNan(OP_ADD),
        1f, operatorNan(OP_ADD),
      ),
    )
  }

  // ── Easing Functions ───────────────────────────────

  @Test
  fun test_easeInQuad_atZero_isZero() {
    assertApprox(0f, eval(0f, operatorNan(OP_EASE_IN_QUAD)))
  }

  @Test
  fun test_easeInQuad_atOne_isOne() {
    assertApprox(1f, eval(1f, operatorNan(OP_EASE_IN_QUAD)))
  }

  @Test
  fun test_easeOutQuad_atHalf() {
    assertApprox(0.75f, eval(0.5f, operatorNan(OP_EASE_OUT_QUAD)))
  }

  @Test
  fun test_easeOutBounce_atOne_isOne() {
    assertApprox(1f, eval(1f, operatorNan(OP_EASE_OUT_BOUNCE)))
  }

  private fun assertApprox(expected: Float, actual: Float, tolerance: Float = 0.001f) {
    assertTrue(
      abs(expected - actual) < tolerance,
      "Expected $expected but got $actual (tolerance $tolerance)",
    )
  }
}
