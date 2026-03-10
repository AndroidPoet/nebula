package io.github.androidpoet.nebula.protocol

import io.github.androidpoet.nebula.protocol.core.*
import io.github.androidpoet.nebula.protocol.creation.*
import io.github.androidpoet.nebula.protocol.operations.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RemoteComposeCreationTest {

  // ── RemoteComposeWriter ────────────────────────────

  @Test
  fun test_writer_headerProducesValidBuffer() {
    val writer = RemoteComposeWriter()
    writer.header()
    val bytes = writer.toByteArray()

    val buf = WireBuffer.fromByteArray(bytes)
    assertEquals(Operations.HEADER, buf.readByte())
    assertEquals(WireBuffer.VERSION, buf.readInt())
  }

  @Test
  fun test_writer_addText_producesValidTextOp() {
    val writer = RemoteComposeWriter()
    writer.header()
    val textId = writer.addText("Hello")

    val bytes = writer.toByteArray()
    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(bytes))

    val ctx = RemoteContext()
    doc.applyDataPass(ctx)
    assertEquals("Hello", ctx.getText(textId))
  }

  @Test
  fun test_writer_addFloat_producesValidDataOp() {
    val writer = RemoteComposeWriter()
    writer.header()
    val id = writer.addFloat(99f)

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(writer.toByteArray()))

    val ctx = RemoteContext()
    doc.applyDataPass(ctx)
    assertEquals(99f, ctx.getFloat(id))
  }

  @Test
  fun test_writer_addColor_producesValidColorOp() {
    val writer = RemoteComposeWriter()
    writer.header()
    val id = writer.addColor(0xFF00FF00)

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(writer.toByteArray()))

    val ctx = RemoteContext()
    doc.applyDataPass(ctx)
    assertEquals(0xFF00FF00, ctx.getColor(id))
  }

  @Test
  fun test_writer_fullLayout_producesValidTree() {
    val writer = RemoteComposeWriter()
    writer.header()
    writer.layoutRoot(400f, 800f)
    val textId = writer.addText("Title")
    writer.layoutColumn(spacing = 16f)
    writer.layoutText(textId)
    writer.containerEnd() // column
    writer.containerEnd() // root

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(writer.toByteArray()))

    assertNotNull(doc.rootComponent)
    assertEquals(1, doc.rootComponent!!.children.size)
    assertTrue(doc.rootComponent!!.children[0] is LayoutColumnOperation)
  }

  // ── RemoteComposeContext (Kotlin DSL) ──────────────

  @Test
  fun test_dsl_remoteDocument_producesValidBytes() {
    val bytes = remoteDocument(width = 400f, height = 800f) {
      val titleText = text("Welcome")
      column(spacing = 16f) {
        layoutText(titleText)
      }
    }

    assertTrue(bytes.isNotEmpty())

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(bytes))

    assertNotNull(doc.rootComponent)
  }

  @Test
  fun test_dsl_textWithStyle_producesValidData() {
    val bytes = remoteDocument(400f, 800f) {
      val style = textStyle {
        fontSize = 24f
        fontWeight = 700
        color = 0xFF000000
        italic = true
      }
      val txt = text("Styled Text")
      layoutText(txt, style)
    }

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(bytes))

    val ctx = RemoteContext()
    doc.applyDataPass(ctx)

    // Find the text in the operation list
    val textOps = doc.operations.filterIsInstance<DataTextOperation>()
    assertTrue(textOps.any { it.text == "Styled Text" })
  }

  @Test
  fun test_dsl_nestedLayout_producesCorrectTree() {
    val bytes = remoteDocument(400f, 800f) {
      column(spacing = 16f) {
        row(spacing = 8f) {
          text("Left")
          text("Right")
        }
        text("Bottom")
      }
    }

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(bytes))

    val root = doc.rootComponent!!
    val column = root.children[0] as LayoutColumnOperation
    // Row + text children, plus data ops (DataText) may be interleaved as children
    assertTrue(column.children.isNotEmpty(), "Column should have children")
  }

  @Test
  fun test_dsl_modifiers_applied() {
    val bytes = remoteDocument(400f, 800f) {
      column {
        modPadding(16f)
        modBackground(0xFF6750A4, 12f)
        text("Modified")
      }
    }

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(bytes))

    // Verify modifiers exist in the operation list
    val paddingOps = doc.operations.filterIsInstance<ModifierPaddingOperation>()
    assertTrue(paddingOps.isNotEmpty())
    assertEquals(16f, paddingOps[0].start)

    val bgOps = doc.operations.filterIsInstance<ModifierBackgroundOperation>()
    assertTrue(bgOps.isNotEmpty())
    assertEquals(0xFF6750A4, bgOps[0].color)
    assertEquals(12f, bgOps[0].cornerRadius)
  }

  @Test
  fun test_dsl_drawCommands_inCanvas() {
    val bytes = remoteDocument(400f, 800f) {
      canvas(width = 200f, height = 200f) {
        paint(color = 0xFFFF0000)
        drawRect(0f, 0f, 100f, 100f)
        drawCircle(50f, 50f, 25f)
      }
    }

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(bytes))

    val drawRects = doc.operations.filterIsInstance<DrawRectOperation>()
    assertTrue(drawRects.isNotEmpty())
    assertEquals(100f, drawRects[0].right)

    val drawCircles = doc.operations.filterIsInstance<DrawCircleOperation>()
    assertTrue(drawCircles.isNotEmpty())
    assertEquals(25f, drawCircles[0].radius)
  }

  @Test
  fun test_dsl_transform_savesAndRestores() {
    val bytes = remoteDocument(400f, 800f) {
      canvas(200f, 200f) {
        withTransform {
          translate(50f, 50f)
          rotate(45f)
          drawRect(0f, 0f, 50f, 50f)
        }
      }
    }

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(bytes))

    val saves = doc.operations.filterIsInstance<MatrixSaveOperation>()
    val restores = doc.operations.filterIsInstance<MatrixRestoreOperation>()
    assertEquals(saves.size, restores.size, "Save/restore should be balanced")
    assertTrue(saves.isNotEmpty())
  }

  @Test
  fun test_dsl_actions_encoded() {
    val bytes = remoteDocument(400f, 800f) {
      column {
        hostNamedAction("navigate", "{\"route\":\"settings\"}")
      }
    }

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(bytes))

    val actions = doc.operations.filterIsInstance<HostNamedActionOperation>()
    assertTrue(actions.isNotEmpty())
    assertEquals("navigate", actions[0].name)
    assertEquals("{\"route\":\"settings\"}", actions[0].metadata)
  }

  @Test
  fun test_dsl_flowLayout_encoded() {
    val bytes = remoteDocument(400f, 800f) {
      flow(mainSpacing = 8f, crossSpacing = 4f) {
        text("Tag 1")
        text("Tag 2")
        text("Tag 3")
      }
    }

    val doc = CoreDocument()
    doc.initFromBuffer(WireBuffer.fromByteArray(bytes))

    val flows = doc.operations.filterIsInstance<LayoutFlowOperation>()
    assertTrue(flows.isNotEmpty())
    assertEquals(8f, flows[0].mainAxisSpacing)
    assertEquals(4f, flows[0].crossAxisSpacing)
  }
}
