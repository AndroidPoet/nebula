package io.github.androidpoet.nebula.protocol

import io.github.androidpoet.nebula.protocol.core.*
import io.github.androidpoet.nebula.protocol.operations.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CoreDocumentTest {

  @Test
  fun test_initFromBuffer_parsesHeader() {
    val buf = WireBuffer()
    HeaderOperation(version = 1, apiLevel = 2, width = 400f, height = 800f).write(buf)

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    assertEquals(1, doc.version)
    assertEquals(2, doc.apiLevel)
  }

  @Test
  fun test_initFromBuffer_parsesLayoutRoot() {
    val buf = WireBuffer()
    HeaderOperation(version = 1, apiLevel = 1, width = 400f, height = 800f).write(buf)
    LayoutRootOperation(width = 400f, height = 800f).write(buf)
    ContainerEndOperation().write(buf)

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    assertNotNull(doc.rootComponent)
    assertTrue(doc.rootComponent is LayoutRootOperation)
  }

  @Test
  fun test_initFromBuffer_inflatesColumnWithChildren() {
    val buf = WireBuffer()
    HeaderOperation(1, 1, 400f, 800f).write(buf)
    LayoutRootOperation(400f, 800f).write(buf)
    LayoutColumnOperation(spacing = 16f).write(buf)
    DataTextOperation(id = 1, text = "Hello").write(buf)
    LayoutTextOperation(textId = 1).write(buf)
    DataTextOperation(id = 2, text = "World").write(buf)
    LayoutTextOperation(textId = 2).write(buf)
    ContainerEndOperation().write(buf) // end column
    ContainerEndOperation().write(buf) // end root

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    val root = doc.rootComponent
    assertNotNull(root)
    assertEquals(1, root.children.size) // column

    val column = root.children[0]
    assertTrue(column is LayoutColumnOperation)
    assertEquals(2, column.children.size) // two text components
  }

  @Test
  fun test_initFromBuffer_parsesNestedLayout() {
    val buf = WireBuffer()
    HeaderOperation(1, 1, 400f, 800f).write(buf)
    LayoutRootOperation(400f, 800f).write(buf)
    LayoutColumnOperation(spacing = 8f).write(buf)
    LayoutRowOperation(spacing = 4f).write(buf)
    LayoutTextOperation(textId = 1).write(buf)
    LayoutTextOperation(textId = 2).write(buf)
    ContainerEndOperation().write(buf) // end row
    LayoutTextOperation(textId = 3).write(buf)
    ContainerEndOperation().write(buf) // end column
    ContainerEndOperation().write(buf) // end root

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    val root = doc.rootComponent!!
    val column = root.children[0] as LayoutColumnOperation
    assertEquals(2, column.children.size) // row + text

    val row = column.children[0] as LayoutRowOperation
    assertEquals(2, row.children.size) // two text components
  }

  @Test
  fun test_dataPass_registersFloatVariables() {
    val buf = WireBuffer()
    HeaderOperation(1, 1, 0f, 0f).write(buf)
    DataFloatOperation(id = 100, value = 42f).write(buf)
    DataFloatOperation(id = 101, value = 3.14f).write(buf)

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    val ctx = RemoteContext()
    doc.applyDataPass(ctx)

    assertEquals(42f, ctx.getFloat(100))
    assertEquals(3.14f, ctx.getFloat(101))
  }

  @Test
  fun test_dataPass_registersTextVariables() {
    val buf = WireBuffer()
    HeaderOperation(1, 1, 0f, 0f).write(buf)
    DataTextOperation(id = 50, text = "Hello Nebula").write(buf)

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    val ctx = RemoteContext()
    doc.applyDataPass(ctx)

    assertEquals("Hello Nebula", ctx.getText(50))
  }

  @Test
  fun test_dataPass_registersColorConstants() {
    val buf = WireBuffer()
    HeaderOperation(1, 1, 0f, 0f).write(buf)
    ColorConstantOperation(id = 10, color = 0xFF6750A4).write(buf)

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    val ctx = RemoteContext()
    doc.applyDataPass(ctx)

    assertEquals(0xFF6750A4, ctx.getColor(10))
  }

  @Test
  fun test_dataPass_registersNamedVariables() {
    val buf = WireBuffer()
    HeaderOperation(1, 1, 0f, 0f).write(buf)
    NamedVariableOperation(name = "score", id = 200, variableType = 0).write(buf)

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    val ctx = RemoteContext()
    doc.applyDataPass(ctx)

    assertEquals(200, ctx.resolveNamedVariable("score"))
  }

  @Test
  fun test_dataPass_registersAnimatedFloat() {
    val buf = WireBuffer()
    HeaderOperation(1, 1, 0f, 0f).write(buf)
    AnimatedFloatOperation(
      id = 300,
      expression = floatArrayOf(42f), // constant expression
      animation = floatArrayOf(),
    ).write(buf)

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    assertTrue(doc.floatExpressions.containsKey(300))
  }

  @Test
  fun test_modifiersAttachedToComponents() {
    val buf = WireBuffer()
    HeaderOperation(1, 1, 400f, 800f).write(buf)
    LayoutRootOperation(400f, 800f).write(buf)
    ModifierPaddingOperation(16f, 16f, 16f, 16f).write(buf) // modifier before component
    LayoutColumnOperation(spacing = 8f).write(buf)
    ContainerEndOperation().write(buf)
    ContainerEndOperation().write(buf)

    val doc = CoreDocument()
    doc.initFromBuffer(buf)

    val root = doc.rootComponent!!
    // The padding modifier should be attached to the root or the column
    // (depends on where it falls in the flat list)
    val totalModifiers = root.modifierOps.size + root.children.sumOf { it.modifierOps.size }
    assertTrue(totalModifiers >= 1, "At least one modifier should be attached")
  }
}
