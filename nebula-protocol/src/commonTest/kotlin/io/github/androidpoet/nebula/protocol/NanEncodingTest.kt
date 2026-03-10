package io.github.androidpoet.nebula.protocol

import io.github.androidpoet.nebula.protocol.core.NanEncoding
import io.github.androidpoet.nebula.protocol.core.NanMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NanEncodingTest {

  @Test
  fun test_asNan_producesNaN() {
    val nan = NanEncoding.asNan(42)
    assertTrue(nan.isNaN())
  }

  @Test
  fun test_idFromNan_extractsOriginalId() {
    val id = 0x123
    val nan = NanEncoding.asNan(id)
    assertEquals(id, NanEncoding.idFromNan(nan))
  }

  @Test
  fun test_roundTrip_allSystemVariableIds() {
    val ids = listOf(
      NanMap.ID_CONTINUOUS_SEC,
      NanMap.ID_DENSITY,
      NanMap.ID_FONT_SCALE,
      NanMap.ID_WINDOW_WIDTH,
      NanMap.ID_WINDOW_HEIGHT,
      NanMap.ID_ANIMATION_TIME,
      NanMap.ID_TOUCH_POS_X,
      NanMap.ID_TOUCH_POS_Y,
      NanMap.ID_TOUCH_DOWN,
      NanMap.ID_ACCELEROMETER_X,
      NanMap.ID_ACCELEROMETER_Y,
      NanMap.ID_ACCELEROMETER_Z,
    )
    for (id in ids) {
      val nan = NanEncoding.asNan(id)
      assertTrue(nan.isNaN(), "ID $id should encode to NaN")
      assertEquals(id, NanEncoding.idFromNan(nan), "ID $id should round-trip")
    }
  }

  @Test
  fun test_roundTrip_userVariableId() {
    val id = NanMap.USER_BASE + 42
    val nan = NanEncoding.asNan(id)
    assertTrue(nan.isNaN())
    assertEquals(id, NanEncoding.idFromNan(nan))
  }

  @Test
  fun test_roundTrip_mathOperatorId() {
    val id = NanMap.MATH_OPERATOR_BASE + 7 // COS
    val nan = NanEncoding.asNan(id)
    assertTrue(nan.isNaN())
    assertEquals(id, NanEncoding.idFromNan(nan))
  }

  @Test
  fun test_isNan_trueForEncoded() {
    // ID 0 encodes to -Infinity (0xFF800000), not NaN — this is by design,
    // actual IDs start from 1
    assertTrue(NanEncoding.isNan(NanEncoding.asNan(1)))
    assertTrue(NanEncoding.isNan(NanEncoding.asNan(0x1FFFFF)))
  }

  @Test
  fun test_isNan_falseForRegularFloats() {
    assertFalse(NanEncoding.isNan(0f))
    assertFalse(NanEncoding.isNan(1f))
    assertFalse(NanEncoding.isNan(-3.14f))
    assertFalse(NanEncoding.isNan(Float.MAX_VALUE))
  }

  @Test
  fun test_resolve_returnsValueForRegularFloat() {
    val store = TestFloatStore()
    assertEquals(3.14f, NanEncoding.resolve(3.14f, store))
    assertEquals(0f, NanEncoding.resolve(0f, store))
  }

  @Test
  fun test_resolve_looksUpNanEncodedId() {
    val store = TestFloatStore()
    store.setFloat(42, 99f)
    val nan = NanEncoding.asNan(42)
    assertEquals(99f, NanEncoding.resolve(nan, store))
  }

  @Test
  fun test_resolve_returnsZeroForMissingVariable() {
    val store = TestFloatStore()
    val nan = NanEncoding.asNan(999)
    assertEquals(0f, NanEncoding.resolve(nan, store))
  }

  @Test
  fun test_nanEncodedFloat_survivesWireBuffer() {
    val buf = WireBuffer()
    val nan = NanEncoding.asNan(NanMap.ID_TOUCH_POS_X)
    buf.writeFloat(nan)
    buf.rewind()
    val decoded = buf.readFloat()
    assertTrue(decoded.isNaN())
    assertEquals(NanMap.ID_TOUCH_POS_X, NanEncoding.idFromNan(decoded))
  }

  @Test
  fun test_preEncodedSystemFloats_haveCorrectIds() {
    assertEquals(NanMap.ID_CONTINUOUS_SEC, NanEncoding.idFromNan(NanMap.FLOAT_CONTINUOUS_SEC))
    assertEquals(NanMap.ID_WINDOW_WIDTH, NanEncoding.idFromNan(NanMap.FLOAT_WINDOW_WIDTH))
    assertEquals(NanMap.ID_TOUCH_POS_X, NanEncoding.idFromNan(NanMap.FLOAT_TOUCH_POS_X))
    assertEquals(NanMap.ID_DENSITY, NanEncoding.idFromNan(NanMap.FLOAT_DENSITY))
  }
}
