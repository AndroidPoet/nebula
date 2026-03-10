package io.github.androidpoet.nebula.protocol.creation

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.NanEncoding
import io.github.androidpoet.nebula.protocol.core.NanMap
import io.github.androidpoet.nebula.protocol.engine.*

/**
 * Kotlin DSL for constructing binary wire protocol documents.
 *
 * Wraps [RemoteComposeWriter] with an idiomatic builder pattern
 * matching the AndroidX RemoteComposeContext API.
 *
 * Usage:
 * ```kotlin
 * val bytes = remoteDocument(width = 400f, height = 800f) {
 *   val titleStyle = textStyle { fontSize = 24f; fontWeight = 700 }
 *   val helloText = text("Hello Nebula")
 *
 *   column(spacing = 16f) {
 *     modPadding(16f)
 *     layoutText(helloText, titleStyle)
 *
 *     row(spacing = 8f) {
 *       button("Get Started") { hostNamedAction("get_started") }
 *       button("Learn More") { hostNamedAction("learn_more") }
 *     }
 *   }
 * }
 * ```
 */
public fun remoteDocument(
  width: Float = 0f,
  height: Float = 0f,
  version: Int = WireBuffer.VERSION,
  apiLevel: Int = 1,
  block: RemoteComposeContext.() -> Unit,
): ByteArray {
  val ctx = RemoteComposeContext()
  ctx.writer.header(version, apiLevel, width, height)
  ctx.writer.layoutRoot(width, height)
  ctx.block()
  ctx.writer.containerEnd()
  return ctx.writer.toByteArray()
}

public class RemoteComposeContext {

  @PublishedApi internal val writer: RemoteComposeWriter = RemoteComposeWriter()

  // ── Data Definitions ───────────────────────────────

  /** Define a text value. Returns the text ID. */
  public fun text(value: String): Int = writer.addText(value)

  /** Define a float constant. Returns the variable ID. */
  public fun float(value: Float): Int = writer.addFloat(value)

  /** Define a color constant. Returns the color ID. */
  public fun color(value: Long): Int = writer.addColor(value)

  /** Define an integer constant. Returns the variable ID. */
  public fun int(value: Int): Int = writer.addInt(value)

  /** Register a named variable. Returns the variable ID. */
  public fun namedVariable(name: String, type: Int = 0): Int = writer.addNamedVariable(name, type)

  /** Define a text style. Returns the style ID. */
  public inline fun textStyle(block: TextStyleScope.() -> Unit): Int {
    val scope = TextStyleScope().apply(block)
    return writer.addTextStyle(
      fontSize = scope.fontSize,
      fontWeight = scope.fontWeight,
      color = scope.color,
      letterSpacing = scope.letterSpacing,
      lineHeight = scope.lineHeight,
      textAlign = scope.textAlign,
      italic = scope.italic,
      decoration = scope.decoration,
    )
  }

  // ── Animated Values ────────────────────────────────

  /** Define an animated float expression. Returns the variable ID. */
  public fun animatedFloat(
    expression: FloatArray,
    animation: FloatArray = floatArrayOf(),
  ): Int = writer.addAnimatedFloat(expression, animation)

  /**
   * Create a reactive float expression using operator overloading.
   *
   * ```kotlin
   * val progress = rFloat { CONTINUOUS_SEC % 2f / 2f }
   * ```
   */
  public fun rFloat(block: RFloatScope.() -> RFloat): Int {
    val scope = RFloatScope()
    val result = scope.block()
    return writer.addAnimatedFloat(result.expression.toFloatArray())
  }

  // ── Paint ──────────────────────────────────────────

  /** Set paint values for subsequent draw commands. */
  public fun paint(
    color: Long = 0xFF000000,
    strokeWidth: Float = 0f,
    style: Int = 0,
    alpha: Float = 1f,
  ) {
    writer.setPaint(color = color, strokeWidth = strokeWidth, style = style, alpha = alpha)
  }

  // ── Draw Commands ──────────────────────────────────

  public fun drawRect(left: Float, top: Float, right: Float, bottom: Float) {
    writer.drawRect(left, top, right, bottom)
  }

  public fun drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float = rx) {
    writer.drawRoundRect(left, top, right, bottom, rx, ry)
  }

  public fun drawCircle(cx: Float, cy: Float, radius: Float) {
    writer.drawCircle(cx, cy, radius)
  }

  public fun drawLine(startX: Float, startY: Float, endX: Float, endY: Float) {
    writer.drawLine(startX, startY, endX, endY)
  }

  public fun drawArc(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, useCenter: Boolean = false) {
    writer.drawArc(left, top, right, bottom, startAngle, sweepAngle, useCenter)
  }

  public fun drawTextRun(textId: Int, x: Float, y: Float, styleId: Int = -1) {
    writer.drawTextRun(textId, x, y, styleId)
  }

  // ── Layout Containers ──────────────────────────────

  /** Column layout (vertical). */
  public inline fun column(
    spacing: Float = 0f,
    horizontalAlignment: Int = 0,
    verticalArrangement: Int = 0,
    block: RemoteComposeContext.() -> Unit = {},
  ) {
    writer.layoutColumn(spacing, horizontalAlignment, verticalArrangement)
    block()
    writer.containerEnd()
  }

  /** Row layout (horizontal). */
  public inline fun row(
    spacing: Float = 0f,
    verticalAlignment: Int = 0,
    horizontalArrangement: Int = 0,
    block: RemoteComposeContext.() -> Unit = {},
  ) {
    writer.layoutRow(spacing, verticalAlignment, horizontalArrangement)
    block()
    writer.containerEnd()
  }

  /** Box layout (overlay/stack). */
  public inline fun box(
    contentAlignment: Int = 0,
    block: RemoteComposeContext.() -> Unit = {},
  ) {
    writer.layoutBox(contentAlignment)
    block()
    writer.containerEnd()
  }

  /** Canvas layout for custom drawing. */
  public inline fun canvas(
    width: Float = 0f,
    height: Float = 0f,
    block: RemoteComposeContext.() -> Unit = {},
  ) {
    writer.layoutCanvas(width, height)
    block()
    writer.containerEnd()
  }

  /** Flow layout (wrapping). */
  public inline fun flow(
    direction: Int = 0,
    mainSpacing: Float = 0f,
    crossSpacing: Float = 0f,
    block: RemoteComposeContext.() -> Unit = {},
  ) {
    writer.layoutFlow(direction, mainSpacing, crossSpacing)
    block()
    writer.containerEnd()
  }

  // ── Leaf Components ────────────────────────────────

  /** Layout text component. */
  public fun layoutText(textId: Int, styleId: Int = -1, maxLines: Int = Int.MAX_VALUE) {
    writer.layoutText(textId, styleId, maxLines)
  }

  /** Layout image component. */
  public fun layoutImage(bitmapId: Int, contentDescription: String = "") {
    writer.layoutImage(bitmapId, contentDescription)
  }

  /** Convenience: define text + layout it in one call. */
  public fun text(value: String, styleId: Int = -1) {
    val id = writer.addText(value)
    writer.layoutText(id, styleId)
  }

  /** Button convenience: text + click action. */
  public inline fun button(label: String, block: RemoteComposeContext.() -> Unit = {}) {
    val textId = writer.addText(label)
    writer.layoutBox()
    writer.modClick(-1)
    writer.layoutText(textId)
    block()
    writer.containerEnd()
  }

  // ── Modifiers ──────────────────────────────────────

  public fun modWidth(width: Float) { writer.modWidth(width) }
  public fun modHeight(height: Float) { writer.modHeight(height) }
  public fun modPadding(all: Float) { writer.modPadding(all) }
  public fun modPadding(start: Float = 0f, top: Float = 0f, end: Float = 0f, bottom: Float = 0f) {
    writer.modPadding(start, top, end, bottom)
  }
  public fun modBackground(color: Long, cornerRadius: Float = 0f) { writer.modBackground(color, cornerRadius) }
  public fun modBorder(width: Float, color: Long, cornerRadius: Float = 0f) { writer.modBorder(width, color, cornerRadius) }
  public fun modClick(actionId: Int, desc: String = "") { writer.modClick(actionId, desc) }
  public fun modScroll(direction: Int = 0) { writer.modScroll(direction) }
  public fun modOffset(x: Float, y: Float) { writer.modOffset(x, y) }
  public fun modVisibility(visible: Boolean) { writer.modVisibility(visible) }

  // ── Transform ──────────────────────────────────────

  /** Apply transforms within a save/restore scope. */
  public inline fun withTransform(block: RemoteComposeContext.() -> Unit) {
    writer.matrixSave()
    block()
    writer.matrixRestore()
  }

  public fun translate(dx: Float, dy: Float) { writer.matrixTranslate(dx, dy) }
  public fun rotate(degrees: Float) { writer.matrixRotate(degrees) }
  public fun scale(sx: Float, sy: Float) { writer.matrixScale(sx, sy) }

  // ── Actions ────────────────────────────────────────

  public fun hostAction(actionId: Int, metadata: String = "") { writer.hostAction(actionId, metadata) }
  public fun hostNamedAction(name: String, metadata: String = "") { writer.hostNamedAction(name, metadata) }

  // ── Theme ──────────────────────────────────────────

  public fun theme(type: Int, colors: Map<Int, Long>) { writer.theme(type, colors) }
}

/** Scope for building text style definitions. */
public class TextStyleScope {
  public var fontSize: Float = 14f
  public var fontWeight: Int = 400
  public var color: Long = 0xFF000000
  public var letterSpacing: Float = 0f
  public var lineHeight: Float = 0f
  public var textAlign: Int = 0
  public var italic: Boolean = false
  public var decoration: Int = 0
}

/**
 * Scope for building reactive float expressions with operator overloading.
 *
 * Produces RPN float arrays that the expression engine evaluates at runtime.
 */
public class RFloatScope {

  public val CONTINUOUS_SEC: RFloat = RFloat(mutableListOf(NanMap.FLOAT_CONTINUOUS_SEC))
  public val ANIMATION_TIME: RFloat = RFloat(mutableListOf(NanMap.FLOAT_ANIMATION_TIME))
  public val WINDOW_WIDTH: RFloat = RFloat(mutableListOf(NanMap.FLOAT_WINDOW_WIDTH))
  public val WINDOW_HEIGHT: RFloat = RFloat(mutableListOf(NanMap.FLOAT_WINDOW_HEIGHT))
  public val TOUCH_X: RFloat = RFloat(mutableListOf(NanMap.FLOAT_TOUCH_POS_X))
  public val TOUCH_Y: RFloat = RFloat(mutableListOf(NanMap.FLOAT_TOUCH_POS_Y))
  public val DENSITY: RFloat = RFloat(mutableListOf(NanMap.FLOAT_DENSITY))

  public fun const(value: Float): RFloat = RFloat(mutableListOf(value))
}

/** Reactive float value that builds an RPN expression. */
public class RFloat(public val expression: MutableList<Float>) {

  public operator fun plus(other: RFloat): RFloat {
    val result = mutableListOf<Float>()
    result.addAll(expression)
    result.addAll(other.expression)
    result.add(operatorNan(OP_ADD))
    return RFloat(result)
  }

  public operator fun minus(other: RFloat): RFloat {
    val result = mutableListOf<Float>()
    result.addAll(expression)
    result.addAll(other.expression)
    result.add(operatorNan(OP_SUB))
    return RFloat(result)
  }

  public operator fun times(other: RFloat): RFloat {
    val result = mutableListOf<Float>()
    result.addAll(expression)
    result.addAll(other.expression)
    result.add(operatorNan(OP_MUL))
    return RFloat(result)
  }

  public operator fun div(other: RFloat): RFloat {
    val result = mutableListOf<Float>()
    result.addAll(expression)
    result.addAll(other.expression)
    result.add(operatorNan(OP_DIV))
    return RFloat(result)
  }

  public operator fun rem(other: RFloat): RFloat {
    val result = mutableListOf<Float>()
    result.addAll(expression)
    result.addAll(other.expression)
    result.add(operatorNan(OP_MOD))
    return RFloat(result)
  }

  public operator fun plus(value: Float): RFloat = plus(RFloat(mutableListOf(value)))
  public operator fun minus(value: Float): RFloat = minus(RFloat(mutableListOf(value)))
  public operator fun times(value: Float): RFloat = times(RFloat(mutableListOf(value)))
  public operator fun div(value: Float): RFloat = div(RFloat(mutableListOf(value)))
  public operator fun rem(value: Float): RFloat = rem(RFloat(mutableListOf(value)))
}
