package io.github.androidpoet.nebula.protocol

import io.github.androidpoet.nebula.protocol.core.Operations
import io.github.androidpoet.nebula.protocol.operations.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OperationsSerializationTest {

  private fun roundTrip(write: (WireBuffer) -> Unit, read: (WireBuffer) -> Unit) {
    val buf = WireBuffer()
    write(buf)
    buf.rewind()
    read(buf)
  }

  // ── Protocol ───────────────────────────────────────

  @Test
  fun test_headerOperation_roundTrips() {
    val op = HeaderOperation(version = 1, apiLevel = 2, width = 400f, height = 800f)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.HEADER, it.readByte())
        val restored = HeaderOperation.read(it)
        assertEquals(1, restored.version)
        assertEquals(2, restored.apiLevel)
        assertEquals(400f, restored.width)
        assertEquals(800f, restored.height)
      },
    )
  }

  // ── Data ───────────────────────────────────────────

  @Test
  fun test_dataFloatOperation_roundTrips() {
    val op = DataFloatOperation(id = 42, value = 3.14f)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.DATA_FLOAT, it.readByte())
        val restored = DataFloatOperation.read(it)
        assertEquals(42, restored.id)
        assertEquals(3.14f, restored.value)
      },
    )
  }

  @Test
  fun test_dataTextOperation_roundTrips() {
    val op = DataTextOperation(id = 10, text = "Hello Nebula")
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.DATA_TEXT, it.readByte())
        val restored = DataTextOperation.read(it)
        assertEquals(10, restored.id)
        assertEquals("Hello Nebula", restored.text)
      },
    )
  }

  @Test
  fun test_colorConstantOperation_roundTrips() {
    val op = ColorConstantOperation(id = 5, color = 0xFF6750A4)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.COLOR_CONSTANT, it.readByte())
        val restored = ColorConstantOperation.read(it)
        assertEquals(5, restored.id)
        assertEquals(0xFF6750A4, restored.color)
      },
    )
  }

  @Test
  fun test_namedVariableOperation_roundTrips() {
    val op = NamedVariableOperation(name = "user.score", id = 100, variableType = 0)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.NAMED_VARIABLE, it.readByte())
        val restored = NamedVariableOperation.read(it)
        assertEquals("user.score", restored.name)
        assertEquals(100, restored.id)
        assertEquals(0, restored.variableType)
      },
    )
  }

  @Test
  fun test_animatedFloatOperation_roundTrips() {
    val expr = floatArrayOf(1f, 2f, 3f)
    val anim = floatArrayOf(0f, 0.3f)
    val op = AnimatedFloatOperation(id = 7, expression = expr, animation = anim)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.ANIMATED_FLOAT, it.readByte())
        val restored = AnimatedFloatOperation.read(it)
        assertEquals(7, restored.id)
        assertEquals(3, restored.expression.size)
        assertEquals(1f, restored.expression[0])
        assertEquals(2, restored.animation.size)
      },
    )
  }

  // ── Draw ───────────────────────────────────────────

  @Test
  fun test_drawRectOperation_roundTrips() {
    val op = DrawRectOperation(left = 10f, top = 20f, right = 100f, bottom = 200f)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.DRAW_RECT, it.readByte())
        val restored = DrawRectOperation.read(it)
        assertEquals(10f, restored.left)
        assertEquals(20f, restored.top)
        assertEquals(100f, restored.right)
        assertEquals(200f, restored.bottom)
      },
    )
  }

  @Test
  fun test_drawCircleOperation_roundTrips() {
    val op = DrawCircleOperation(centerX = 50f, centerY = 50f, radius = 25f)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.DRAW_CIRCLE, it.readByte())
        val restored = DrawCircleOperation.read(it)
        assertEquals(50f, restored.centerX)
        assertEquals(50f, restored.centerY)
        assertEquals(25f, restored.radius)
      },
    )
  }

  @Test
  fun test_drawArcOperation_roundTrips() {
    val op = DrawArcOperation(10f, 10f, 100f, 100f, 0f, 90f, true)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.DRAW_ARC, it.readByte())
        val restored = DrawArcOperation.read(it)
        assertEquals(0f, restored.startAngle)
        assertEquals(90f, restored.sweepAngle)
        assertTrue(restored.useCenter)
      },
    )
  }

  @Test
  fun test_paintValuesOperation_roundTrips() {
    val op = PaintValuesOperation(
      color = 0xFFFF0000, strokeWidth = 2f,
      style = PaintValuesOperation.STYLE_STROKE,
      strokeCap = PaintValuesOperation.CAP_ROUND,
      strokeJoin = PaintValuesOperation.JOIN_ROUND,
      alpha = 0.8f, blendMode = 0,
    )
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.PAINT_VALUES, it.readByte())
        val restored = PaintValuesOperation.read(it)
        assertEquals(0xFFFF0000, restored.color)
        assertEquals(2f, restored.strokeWidth)
        assertEquals(PaintValuesOperation.STYLE_STROKE, restored.style)
        assertEquals(PaintValuesOperation.CAP_ROUND, restored.strokeCap)
        assertEquals(0.8f, restored.alpha)
      },
    )
  }

  // ── Transform ──────────────────────────────────────

  @Test
  fun test_matrixTranslateOperation_roundTrips() {
    val op = MatrixTranslateOperation(dx = 10f, dy = 20f)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.MATRIX_TRANSLATE, it.readByte())
        val restored = MatrixTranslateOperation.read(it)
        assertEquals(10f, restored.dx)
        assertEquals(20f, restored.dy)
      },
    )
  }

  @Test
  fun test_matrixRotateOperation_roundTrips() {
    val op = MatrixRotateOperation(degrees = 45f, pivotX = 100f, pivotY = 100f)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.MATRIX_ROTATE, it.readByte())
        val restored = MatrixRotateOperation.read(it)
        assertEquals(45f, restored.degrees)
        assertEquals(100f, restored.pivotX)
        assertEquals(100f, restored.pivotY)
      },
    )
  }

  // ── Layout ─────────────────────────────────────────

  @Test
  fun test_layoutColumnOperation_roundTrips() {
    val op = LayoutColumnOperation(spacing = 16f, horizontalAlignment = 1, verticalArrangement = 3)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.LAYOUT_COLUMN, it.readByte())
        val restored = LayoutColumnOperation.read(it)
        assertEquals(16f, restored.spacing)
        assertEquals(1, restored.horizontalAlignment)
        assertEquals(3, restored.verticalArrangement)
      },
    )
  }

  @Test
  fun test_layoutRowOperation_roundTrips() {
    val op = LayoutRowOperation(spacing = 8f, verticalAlignment = 1, horizontalArrangement = 5)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.LAYOUT_ROW, it.readByte())
        val restored = LayoutRowOperation.read(it)
        assertEquals(8f, restored.spacing)
        assertEquals(1, restored.verticalAlignment)
        assertEquals(5, restored.horizontalArrangement)
      },
    )
  }

  @Test
  fun test_layoutTextOperation_roundTrips() {
    val op = LayoutTextOperation(textId = 42, styleId = 3, maxLines = 2, overflow = 1)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.LAYOUT_TEXT, it.readByte())
        val restored = LayoutTextOperation.read(it)
        assertEquals(42, restored.textId)
        assertEquals(3, restored.styleId)
        assertEquals(2, restored.maxLines)
        assertEquals(1, restored.overflow)
      },
    )
  }

  // ── Modifiers ──────────────────────────────────────

  @Test
  fun test_modifierPaddingOperation_roundTrips() {
    val op = ModifierPaddingOperation(start = 8f, top = 16f, end = 8f, bottom = 16f)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.MODIFIER_PADDING, it.readByte())
        val restored = ModifierPaddingOperation.read(it)
        assertEquals(8f, restored.start)
        assertEquals(16f, restored.top)
        assertEquals(8f, restored.end)
        assertEquals(16f, restored.bottom)
      },
    )
  }

  @Test
  fun test_modifierBackgroundOperation_roundTrips() {
    val op = ModifierBackgroundOperation(color = 0xFF6750A4, cornerRadius = 12f)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.MODIFIER_BACKGROUND, it.readByte())
        val restored = ModifierBackgroundOperation.read(it)
        assertEquals(0xFF6750A4, restored.color)
        assertEquals(12f, restored.cornerRadius)
      },
    )
  }

  @Test
  fun test_modifierGraphicsLayerOperation_roundTrips() {
    val op = ModifierGraphicsLayerOperation(
      alpha = 0.5f, rotationZ = 45f, scaleX = 2f, scaleY = 2f, shadowElevation = 8f,
    )
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.MODIFIER_GRAPHICS_LAYER, it.readByte())
        val restored = ModifierGraphicsLayerOperation.read(it)
        assertEquals(0.5f, restored.alpha)
        assertEquals(45f, restored.rotationZ)
        assertEquals(2f, restored.scaleX)
        assertEquals(8f, restored.shadowElevation)
      },
    )
  }

  @Test
  fun test_modifierClickOperation_roundTrips() {
    val op = ModifierClickOperation(actionId = 99, contentDescription = "Tap me")
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.MODIFIER_CLICK, it.readByte())
        val restored = ModifierClickOperation.read(it)
        assertEquals(99, restored.actionId)
        assertEquals("Tap me", restored.contentDescription)
      },
    )
  }

  // ── Actions ────────────────────────────────────────

  @Test
  fun test_hostNamedActionOperation_roundTrips() {
    val op = HostNamedActionOperation(name = "navigate", metadata = "{\"route\":\"home\"}")
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.HOST_NAMED_ACTION, it.readByte())
        val restored = HostNamedActionOperation.read(it)
        assertEquals("navigate", restored.name)
        assertEquals("{\"route\":\"home\"}", restored.metadata)
      },
    )
  }

  @Test
  fun test_conditionalOperation_roundTrips() {
    val innerData = byteArrayOf(1, 2, 3, 4)
    val op = ConditionalOperation(variableId = 5, compareValue = 1, compareOp = 0, operationData = innerData)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.CONDITIONAL_OPERATIONS, it.readByte())
        val restored = ConditionalOperation.read(it)
        assertEquals(5, restored.variableId)
        assertEquals(1, restored.compareValue)
        assertEquals(0, restored.compareOp)
        assertTrue(innerData.contentEquals(restored.operationData))
      },
    )
  }

  @Test
  fun test_loopStartOperation_roundTrips() {
    val op = LoopStartOperation(count = 10, variableId = 3)
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.LOOP_START, it.readByte())
        val restored = LoopStartOperation.read(it)
        assertEquals(10, restored.count)
        assertEquals(3, restored.variableId)
      },
    )
  }

  // ── Text Style ─────────────────────────────────────

  @Test
  fun test_textStyleOperation_roundTrips() {
    val op = TextStyleOperation(
      styleId = 1, fontSize = 24f, fontWeight = 700, color = 0xFF000000,
      letterSpacing = 0.5f, lineHeight = 32f, textAlign = 1, italic = true, decoration = 1,
    )
    roundTrip(
      write = { op.write(it) },
      read = {
        assertEquals(Operations.TEXT_STYLE, it.readByte())
        val restored = TextStyleOperation.read(it)
        assertEquals(1, restored.styleId)
        assertEquals(24f, restored.fontSize)
        assertEquals(700, restored.fontWeight)
        assertEquals(0xFF000000, restored.color)
        assertEquals(0.5f, restored.letterSpacing)
        assertEquals(32f, restored.lineHeight)
        assertEquals(1, restored.textAlign)
        assertTrue(restored.italic)
        assertEquals(1, restored.decoration)
      },
    )
  }
}
