package io.github.androidpoet.nebula.protocol.operations

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.*

/** Paint state values applied before draw commands. */
public class PaintValuesOperation(
  public val color: Long,
  public val strokeWidth: Float,
  public val style: Int,
  public val strokeCap: Int,
  public val strokeJoin: Int,
  public val alpha: Float,
  public val blendMode: Int,
) : Operation(Operations.PAINT_VALUES) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.PAINT_VALUES)
    buffer.writeColor(color)
    buffer.writeFloat(strokeWidth)
    buffer.writeInt(style)
    buffer.writeInt(strokeCap)
    buffer.writeInt(strokeJoin)
    buffer.writeFloat(alpha)
    buffer.writeInt(blendMode)
  }

  public companion object {
    public const val STYLE_FILL: Int = 0
    public const val STYLE_STROKE: Int = 1
    public const val STYLE_FILL_AND_STROKE: Int = 2

    public const val CAP_BUTT: Int = 0
    public const val CAP_ROUND: Int = 1
    public const val CAP_SQUARE: Int = 2

    public const val JOIN_MITER: Int = 0
    public const val JOIN_ROUND: Int = 1
    public const val JOIN_BEVEL: Int = 2

    public fun read(buffer: WireBuffer): PaintValuesOperation {
      return PaintValuesOperation(
        color = buffer.readColor(),
        strokeWidth = buffer.readFloat(),
        style = buffer.readInt(),
        strokeCap = buffer.readInt(),
        strokeJoin = buffer.readInt(),
        alpha = buffer.readFloat(),
        blendMode = buffer.readInt(),
      )
    }
  }
}

/** Draw a filled/stroked rectangle. */
public class DrawRectOperation(
  public val left: Float,
  public val top: Float,
  public val right: Float,
  public val bottom: Float,
) : Operation(Operations.DRAW_RECT) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_RECT)
    buffer.writeFloat(left)
    buffer.writeFloat(top)
    buffer.writeFloat(right)
    buffer.writeFloat(bottom)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawRectOperation {
      return DrawRectOperation(
        left = buffer.readFloat(), top = buffer.readFloat(),
        right = buffer.readFloat(), bottom = buffer.readFloat(),
      )
    }
  }
}

/** Draw a rounded rectangle. */
public class DrawRoundRectOperation(
  public val left: Float,
  public val top: Float,
  public val right: Float,
  public val bottom: Float,
  public val radiusX: Float,
  public val radiusY: Float,
) : Operation(Operations.DRAW_ROUND_RECT) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_ROUND_RECT)
    buffer.writeFloat(left)
    buffer.writeFloat(top)
    buffer.writeFloat(right)
    buffer.writeFloat(bottom)
    buffer.writeFloat(radiusX)
    buffer.writeFloat(radiusY)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawRoundRectOperation {
      return DrawRoundRectOperation(
        left = buffer.readFloat(), top = buffer.readFloat(),
        right = buffer.readFloat(), bottom = buffer.readFloat(),
        radiusX = buffer.readFloat(), radiusY = buffer.readFloat(),
      )
    }
  }
}

/** Draw a circle. */
public class DrawCircleOperation(
  public val centerX: Float,
  public val centerY: Float,
  public val radius: Float,
) : Operation(Operations.DRAW_CIRCLE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_CIRCLE)
    buffer.writeFloat(centerX)
    buffer.writeFloat(centerY)
    buffer.writeFloat(radius)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawCircleOperation {
      return DrawCircleOperation(
        centerX = buffer.readFloat(),
        centerY = buffer.readFloat(),
        radius = buffer.readFloat(),
      )
    }
  }
}

/** Draw an oval inscribed in bounds. */
public class DrawOvalOperation(
  public val left: Float,
  public val top: Float,
  public val right: Float,
  public val bottom: Float,
) : Operation(Operations.DRAW_OVAL) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_OVAL)
    buffer.writeFloat(left)
    buffer.writeFloat(top)
    buffer.writeFloat(right)
    buffer.writeFloat(bottom)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawOvalOperation {
      return DrawOvalOperation(
        left = buffer.readFloat(), top = buffer.readFloat(),
        right = buffer.readFloat(), bottom = buffer.readFloat(),
      )
    }
  }
}

/** Draw a line segment. */
public class DrawLineOperation(
  public val startX: Float,
  public val startY: Float,
  public val endX: Float,
  public val endY: Float,
) : Operation(Operations.DRAW_LINE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_LINE)
    buffer.writeFloat(startX)
    buffer.writeFloat(startY)
    buffer.writeFloat(endX)
    buffer.writeFloat(endY)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawLineOperation {
      return DrawLineOperation(
        startX = buffer.readFloat(), startY = buffer.readFloat(),
        endX = buffer.readFloat(), endY = buffer.readFloat(),
      )
    }
  }
}

/** Draw an arc within a bounding oval. */
public class DrawArcOperation(
  public val left: Float,
  public val top: Float,
  public val right: Float,
  public val bottom: Float,
  public val startAngle: Float,
  public val sweepAngle: Float,
  public val useCenter: Boolean,
) : Operation(Operations.DRAW_ARC) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_ARC)
    buffer.writeFloat(left)
    buffer.writeFloat(top)
    buffer.writeFloat(right)
    buffer.writeFloat(bottom)
    buffer.writeFloat(startAngle)
    buffer.writeFloat(sweepAngle)
    buffer.writeBoolean(useCenter)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawArcOperation {
      return DrawArcOperation(
        left = buffer.readFloat(), top = buffer.readFloat(),
        right = buffer.readFloat(), bottom = buffer.readFloat(),
        startAngle = buffer.readFloat(), sweepAngle = buffer.readFloat(),
        useCenter = buffer.readBoolean(),
      )
    }
  }
}

/** Draw a path from serialized path data. */
public class DrawPathOperation(
  public val pathId: Int,
) : Operation(Operations.DRAW_PATH) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_PATH)
    buffer.writeInt(pathId)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawPathOperation {
      return DrawPathOperation(pathId = buffer.readInt())
    }
  }
}

/** Draw a bitmap at a position. */
public class DrawBitmapOperation(
  public val bitmapId: Int,
  public val srcLeft: Float,
  public val srcTop: Float,
  public val srcRight: Float,
  public val srcBottom: Float,
  public val dstLeft: Float,
  public val dstTop: Float,
  public val dstRight: Float,
  public val dstBottom: Float,
) : Operation(Operations.DRAW_BITMAP) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_BITMAP)
    buffer.writeInt(bitmapId)
    buffer.writeFloat(srcLeft); buffer.writeFloat(srcTop)
    buffer.writeFloat(srcRight); buffer.writeFloat(srcBottom)
    buffer.writeFloat(dstLeft); buffer.writeFloat(dstTop)
    buffer.writeFloat(dstRight); buffer.writeFloat(dstBottom)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawBitmapOperation {
      return DrawBitmapOperation(
        bitmapId = buffer.readInt(),
        srcLeft = buffer.readFloat(), srcTop = buffer.readFloat(),
        srcRight = buffer.readFloat(), srcBottom = buffer.readFloat(),
        dstLeft = buffer.readFloat(), dstTop = buffer.readFloat(),
        dstRight = buffer.readFloat(), dstBottom = buffer.readFloat(),
      )
    }
  }
}

/** Draw a text run. */
public class DrawTextRunOperation(
  public val textId: Int,
  public val x: Float,
  public val y: Float,
  public val styleId: Int,
) : Operation(Operations.DRAW_TEXT_RUN) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_TEXT_RUN)
    buffer.writeInt(textId)
    buffer.writeFloat(x)
    buffer.writeFloat(y)
    buffer.writeInt(styleId)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawTextRunOperation {
      return DrawTextRunOperation(
        textId = buffer.readInt(),
        x = buffer.readFloat(), y = buffer.readFloat(),
        styleId = buffer.readInt(),
      )
    }
  }
}

/** Draw content marker (slot for child content). */
public class DrawContentOperation : Operation(Operations.DRAW_CONTENT) {
  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_CONTENT)
  }
}

/** Draw anchored text with alignment. */
public class DrawTextAnchorOperation(
  public val textId: Int,
  public val x: Float,
  public val y: Float,
  public val anchorX: Float,
  public val anchorY: Float,
  public val styleId: Int,
) : Operation(Operations.DRAW_TEXT_ANCHOR) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DRAW_TEXT_ANCHOR)
    buffer.writeInt(textId)
    buffer.writeFloat(x)
    buffer.writeFloat(y)
    buffer.writeFloat(anchorX)
    buffer.writeFloat(anchorY)
    buffer.writeInt(styleId)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DrawTextAnchorOperation {
      return DrawTextAnchorOperation(
        textId = buffer.readInt(),
        x = buffer.readFloat(), y = buffer.readFloat(),
        anchorX = buffer.readFloat(), anchorY = buffer.readFloat(),
        styleId = buffer.readInt(),
      )
    }
  }
}

/** Clip to a rectangle. */
public class ClipRectOperation(
  public val left: Float,
  public val top: Float,
  public val right: Float,
  public val bottom: Float,
) : Operation(Operations.CLIP_RECT) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.CLIP_RECT)
    buffer.writeFloat(left)
    buffer.writeFloat(top)
    buffer.writeFloat(right)
    buffer.writeFloat(bottom)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ClipRectOperation {
      return ClipRectOperation(
        left = buffer.readFloat(), top = buffer.readFloat(),
        right = buffer.readFloat(), bottom = buffer.readFloat(),
      )
    }
  }
}

/** Clip to a path. */
public class ClipPathOperation(
  public val pathId: Int,
) : Operation(Operations.CLIP_PATH) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.CLIP_PATH)
    buffer.writeInt(pathId)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ClipPathOperation {
      return ClipPathOperation(pathId = buffer.readInt())
    }
  }
}

/** Canvas operations container. */
public class CanvasOperationsOp(
  public val operationData: ByteArray,
) : Operation(Operations.CANVAS_OPERATIONS) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.CANVAS_OPERATIONS)
    buffer.writeBytes(operationData)
  }

  public companion object {
    public fun read(buffer: WireBuffer): CanvasOperationsOp {
      return CanvasOperationsOp(operationData = buffer.readBytes())
    }
  }
}
