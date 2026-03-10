package io.github.androidpoet.nebula.protocol

import io.github.androidpoet.nebula.protocol.core.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemoteContextTest {

  @Test
  fun test_systemVariables_resolveCorrectly() {
    val ctx = RemoteContext()
    ctx.continuousTimeSec = 5.5f
    ctx.density = 2f
    ctx.windowWidth = 1080f
    ctx.windowHeight = 1920f
    ctx.touchX = 540f
    ctx.touchY = 960f

    assertEquals(5.5f, ctx.getFloat(NanMap.ID_CONTINUOUS_SEC))
    assertEquals(2f, ctx.getFloat(NanMap.ID_DENSITY))
    assertEquals(1080f, ctx.getFloat(NanMap.ID_WINDOW_WIDTH))
    assertEquals(1920f, ctx.getFloat(NanMap.ID_WINDOW_HEIGHT))
    assertEquals(540f, ctx.getFloat(NanMap.ID_TOUCH_POS_X))
    assertEquals(960f, ctx.getFloat(NanMap.ID_TOUCH_POS_Y))
  }

  @Test
  fun test_userVariables_setAndGet() {
    val ctx = RemoteContext()
    ctx.setFloat(NanMap.USER_BASE + 1, 42f)
    ctx.setFloat(NanMap.USER_BASE + 2, 99f)

    assertEquals(42f, ctx.getFloat(NanMap.USER_BASE + 1))
    assertEquals(99f, ctx.getFloat(NanMap.USER_BASE + 2))
  }

  @Test
  fun test_colorVariables_loadAndGet() {
    val ctx = RemoteContext()
    ctx.loadColor(10, 0xFF6750A4)

    assertEquals(0xFF6750A4, ctx.getColor(10))
  }

  @Test
  fun test_textVariables_loadAndGet() {
    val ctx = RemoteContext()
    ctx.loadText(20, "Hello Nebula")

    assertEquals("Hello Nebula", ctx.getText(20))
  }

  @Test
  fun test_intVariables_loadAndGet() {
    val ctx = RemoteContext()
    ctx.loadInt(30, 42)

    assertEquals(42, ctx.getInt(30))
  }

  @Test
  fun test_booleanVariables_loadAndGet() {
    val ctx = RemoteContext()
    ctx.loadBoolean(40, true)

    assertTrue(ctx.getBoolean(40))
  }

  @Test
  fun test_namedVariables_registerAndResolve() {
    val ctx = RemoteContext()
    ctx.registerNamedVariable("score", 100)

    assertEquals(100, ctx.resolveNamedVariable("score"))
    assertNull(ctx.resolveNamedVariable("nonexistent"))
  }

  @Test
  fun test_resolveFloat_regularValue_passesThrough() {
    val ctx = RemoteContext()
    assertEquals(3.14f, ctx.resolveFloat(3.14f))
    assertEquals(0f, ctx.resolveFloat(0f))
  }

  @Test
  fun test_resolveFloat_nanValue_looksUpVariable() {
    val ctx = RemoteContext()
    ctx.setFloat(NanMap.USER_BASE + 5, 42f)

    val nan = NanEncoding.asNan(NanMap.USER_BASE + 5)
    assertEquals(42f, ctx.resolveFloat(nan))
  }

  @Test
  fun test_resolveFloat_systemVariable_viaNan() {
    val ctx = RemoteContext()
    ctx.windowWidth = 1080f

    val nan = NanMap.FLOAT_WINDOW_WIDTH
    assertEquals(1080f, ctx.resolveFloat(nan))
  }

  @Test
  fun test_defaultValues_returnSensibleDefaults() {
    val ctx = RemoteContext()

    assertEquals(0f, ctx.getFloat(99999))
    assertEquals(0xFF000000, ctx.getColor(99999))
    assertEquals("", ctx.getText(99999))
    assertEquals(0, ctx.getInt(99999))
    assertEquals(false, ctx.getBoolean(99999))
  }

  @Test
  fun test_contextMode_startsUnset() {
    val ctx = RemoteContext()
    assertEquals(ContextMode.UNSET, ctx.mode)
  }
}
