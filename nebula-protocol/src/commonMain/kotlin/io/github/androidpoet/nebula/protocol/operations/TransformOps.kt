package io.github.androidpoet.nebula.protocol.operations

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.*

/** Save the current matrix state. */
public class MatrixSaveOperation : Operation(Operations.MATRIX_SAVE) {
  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MATRIX_SAVE)
  }
}

/** Restore the previously saved matrix state. */
public class MatrixRestoreOperation : Operation(Operations.MATRIX_RESTORE) {
  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MATRIX_RESTORE)
  }
}

/** Translate the canvas origin. */
public class MatrixTranslateOperation(
  public val dx: Float,
  public val dy: Float,
) : Operation(Operations.MATRIX_TRANSLATE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MATRIX_TRANSLATE)
    buffer.writeFloat(dx)
    buffer.writeFloat(dy)
  }

  public companion object {
    public fun read(buffer: WireBuffer): MatrixTranslateOperation {
      return MatrixTranslateOperation(dx = buffer.readFloat(), dy = buffer.readFloat())
    }
  }
}

/** Rotate the canvas. */
public class MatrixRotateOperation(
  public val degrees: Float,
  public val pivotX: Float = 0f,
  public val pivotY: Float = 0f,
) : Operation(Operations.MATRIX_ROTATE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MATRIX_ROTATE)
    buffer.writeFloat(degrees)
    buffer.writeFloat(pivotX)
    buffer.writeFloat(pivotY)
  }

  public companion object {
    public fun read(buffer: WireBuffer): MatrixRotateOperation {
      return MatrixRotateOperation(
        degrees = buffer.readFloat(),
        pivotX = buffer.readFloat(),
        pivotY = buffer.readFloat(),
      )
    }
  }
}

/** Scale the canvas. */
public class MatrixScaleOperation(
  public val sx: Float,
  public val sy: Float,
  public val pivotX: Float = 0f,
  public val pivotY: Float = 0f,
) : Operation(Operations.MATRIX_SCALE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MATRIX_SCALE)
    buffer.writeFloat(sx)
    buffer.writeFloat(sy)
    buffer.writeFloat(pivotX)
    buffer.writeFloat(pivotY)
  }

  public companion object {
    public fun read(buffer: WireBuffer): MatrixScaleOperation {
      return MatrixScaleOperation(
        sx = buffer.readFloat(), sy = buffer.readFloat(),
        pivotX = buffer.readFloat(), pivotY = buffer.readFloat(),
      )
    }
  }
}

/** Skew the canvas. */
public class MatrixSkewOperation(
  public val skewX: Float,
  public val skewY: Float,
) : Operation(Operations.MATRIX_SKEW) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.MATRIX_SKEW)
    buffer.writeFloat(skewX)
    buffer.writeFloat(skewY)
  }

  public companion object {
    public fun read(buffer: WireBuffer): MatrixSkewOperation {
      return MatrixSkewOperation(skewX = buffer.readFloat(), skewY = buffer.readFloat())
    }
  }
}
