package io.github.androidpoet.nebula.protocol.creation

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.ContainerEndOperation
import io.github.androidpoet.nebula.protocol.core.NanMap
import io.github.androidpoet.nebula.protocol.core.Operations
import io.github.androidpoet.nebula.protocol.engine.*
import io.github.androidpoet.nebula.protocol.operations.*

/**
 * Low-level writer for constructing binary wire protocol documents.
 *
 * Wraps a [WireBuffer] and provides methods for every operation type.
 * For a higher-level Kotlin DSL, see [RemoteComposeContext].
 */
public class RemoteComposeWriter(
  public val buffer: WireBuffer = WireBuffer(),
) {

  private var nextUserId = NanMap.USER_BASE
  private var nextTextId = NanMap.USER_BASE + 0x10000
  private var nextStyleId = 1

  /** Write the document header. */
  public fun header(
    version: Int = WireBuffer.VERSION,
    apiLevel: Int = 1,
    width: Float = 0f,
    height: Float = 0f,
  ) {
    HeaderOperation(version, apiLevel, width, height).write(buffer)
  }

  // ── Data Definitions ───────────────────────────────

  /** Define a constant float value. Returns the variable ID. */
  public fun addFloat(value: Float): Int {
    val id = nextUserId++
    DataFloatOperation(id, value).write(buffer)
    return id
  }

  /** Define a constant float with an explicit ID. */
  public fun addFloat(id: Int, value: Float) {
    DataFloatOperation(id, value).write(buffer)
  }

  /** Define an animated float expression. Returns the variable ID. */
  public fun addAnimatedFloat(
    expression: FloatArray,
    animation: FloatArray = floatArrayOf(),
  ): Int {
    val id = nextUserId++
    AnimatedFloatOperation(id, expression, animation).write(buffer)
    return id
  }

  /** Define a text value. Returns the text ID. */
  public fun addText(text: String): Int {
    val id = nextTextId++
    DataTextOperation(id, text).write(buffer)
    return id
  }

  /** Define an integer value. Returns the variable ID. */
  public fun addInt(value: Int): Int {
    val id = nextUserId++
    DataIntOperation(id, value).write(buffer)
    return id
  }

  /** Define a color constant. Returns the color ID. */
  public fun addColor(color: Long): Int {
    val id = nextUserId++
    ColorConstantOperation(id, color).write(buffer)
    return id
  }

  /** Register a named variable. */
  public fun addNamedVariable(name: String, type: Int = NamedVariableOperation.TYPE_FLOAT): Int {
    val id = nextUserId++
    NamedVariableOperation(name, id, type).write(buffer)
    return id
  }

  // ── Text Style ─────────────────────────────────────

  /** Define a text style. Returns the style ID. */
  public fun addTextStyle(
    fontSize: Float = 14f,
    fontWeight: Int = 400,
    color: Long = 0xFF000000,
    letterSpacing: Float = 0f,
    lineHeight: Float = 0f,
    textAlign: Int = 0,
    italic: Boolean = false,
    decoration: Int = 0,
  ): Int {
    val id = nextStyleId++
    TextStyleOperation(id, fontSize, fontWeight, color, letterSpacing, lineHeight, textAlign, italic, decoration).write(buffer)
    return id
  }

  // ── Paint State ────────────────────────────────────

  /** Set paint values for subsequent draw operations. */
  public fun setPaint(
    color: Long = 0xFF000000,
    strokeWidth: Float = 0f,
    style: Int = PaintValuesOperation.STYLE_FILL,
    strokeCap: Int = PaintValuesOperation.CAP_BUTT,
    strokeJoin: Int = PaintValuesOperation.JOIN_MITER,
    alpha: Float = 1f,
    blendMode: Int = 0,
  ) {
    PaintValuesOperation(color, strokeWidth, style, strokeCap, strokeJoin, alpha, blendMode).write(buffer)
  }

  // ── Draw Commands ──────────────────────────────────

  public fun drawRect(left: Float, top: Float, right: Float, bottom: Float) {
    DrawRectOperation(left, top, right, bottom).write(buffer)
  }

  public fun drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float) {
    DrawRoundRectOperation(left, top, right, bottom, rx, ry).write(buffer)
  }

  public fun drawCircle(cx: Float, cy: Float, radius: Float) {
    DrawCircleOperation(cx, cy, radius).write(buffer)
  }

  public fun drawOval(left: Float, top: Float, right: Float, bottom: Float) {
    DrawOvalOperation(left, top, right, bottom).write(buffer)
  }

  public fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float) {
    DrawLineOperation(startX, startY, endX, endY).write(buffer)
  }

  public fun drawArc(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, useCenter: Boolean) {
    DrawArcOperation(left, top, right, bottom, startAngle, sweepAngle, useCenter).write(buffer)
  }

  public fun drawTextRun(textId: Int, x: Float, y: Float, styleId: Int = -1) {
    DrawTextRunOperation(textId, x, y, styleId).write(buffer)
  }

  // ── Matrix / Transform ─────────────────────────────

  public fun matrixSave() { MatrixSaveOperation().write(buffer) }
  public fun matrixRestore() { MatrixRestoreOperation().write(buffer) }
  public fun matrixTranslate(dx: Float, dy: Float) { MatrixTranslateOperation(dx, dy).write(buffer) }
  public fun matrixRotate(degrees: Float, pivotX: Float = 0f, pivotY: Float = 0f) { MatrixRotateOperation(degrees, pivotX, pivotY).write(buffer) }
  public fun matrixScale(sx: Float, sy: Float, pivotX: Float = 0f, pivotY: Float = 0f) { MatrixScaleOperation(sx, sy, pivotX, pivotY).write(buffer) }

  // ── Layout ─────────────────────────────────────────

  public fun layoutRoot(width: Float, height: Float) { LayoutRootOperation(width, height).write(buffer) }
  public fun layoutBox(contentAlignment: Int = 0) { LayoutBoxOperation(contentAlignment).write(buffer) }
  public fun layoutRow(spacing: Float = 0f, vAlign: Int = 0, hArrangement: Int = 0) { LayoutRowOperation(spacing, vAlign, hArrangement).write(buffer) }
  public fun layoutColumn(spacing: Float = 0f, hAlign: Int = 0, vArrangement: Int = 0) { LayoutColumnOperation(spacing, hAlign, vArrangement).write(buffer) }
  public fun layoutText(textId: Int, styleId: Int = -1, maxLines: Int = Int.MAX_VALUE) { LayoutTextOperation(textId, styleId, maxLines).write(buffer) }
  public fun layoutImage(bitmapId: Int, contentDescription: String = "") { LayoutImageOperation(bitmapId, contentDescription).write(buffer) }
  public fun layoutCanvas(width: Float = 0f, height: Float = 0f) { LayoutCanvasOperation(width, height).write(buffer) }
  public fun layoutFlow(direction: Int = 0, mainSpacing: Float = 0f, crossSpacing: Float = 0f) { LayoutFlowOperation(direction, mainSpacing, crossSpacing).write(buffer) }
  public fun containerEnd() { ContainerEndOperation().write(buffer) }

  // ── Modifiers ──────────────────────────────────────

  public fun modWidth(width: Float) { ModifierWidthOperation(width).write(buffer) }
  public fun modHeight(height: Float) { ModifierHeightOperation(height).write(buffer) }
  public fun modPadding(start: Float = 0f, top: Float = 0f, end: Float = 0f, bottom: Float = 0f) { ModifierPaddingOperation(start, top, end, bottom).write(buffer) }
  public fun modPadding(all: Float) { modPadding(all, all, all, all) }
  public fun modBackground(color: Long, cornerRadius: Float = 0f) { ModifierBackgroundOperation(color, cornerRadius).write(buffer) }
  public fun modBorder(width: Float, color: Long, cornerRadius: Float = 0f) { ModifierBorderOperation(width, color, cornerRadius).write(buffer) }
  public fun modClick(actionId: Int, desc: String = "") { ModifierClickOperation(actionId, desc).write(buffer) }
  public fun modScroll(direction: Int = 0) { ModifierScrollOperation(direction).write(buffer) }
  public fun modOffset(x: Float, y: Float) { ModifierOffsetOperation(x, y).write(buffer) }
  public fun modVisibility(visible: Boolean) { ModifierVisibilityOperation(visible).write(buffer) }
  public fun modGraphicsLayer(
    alpha: Float = 1f, rotX: Float = 0f, rotY: Float = 0f, rotZ: Float = 0f,
    scaleX: Float = 1f, scaleY: Float = 1f, transX: Float = 0f, transY: Float = 0f,
    shadow: Float = 0f,
  ) {
    ModifierGraphicsLayerOperation(alpha, rotX, rotY, rotZ, scaleX, scaleY, transX, transY, shadow).write(buffer)
  }

  // ── Actions ────────────────────────────────────────

  public fun hostAction(actionId: Int, metadata: String = "") { HostActionOperation(actionId, metadata).write(buffer) }
  public fun hostNamedAction(name: String, metadata: String = "") { HostNamedActionOperation(name, metadata).write(buffer) }

  // ── Theme ──────────────────────────────────────────

  public fun theme(type: Int, colors: Map<Int, Long>) { ThemeOperation(type, colors).write(buffer) }

  /** Get the serialized byte array. */
  public fun toByteArray(): ByteArray = buffer.toByteArray()
}
