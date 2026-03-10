package io.github.androidpoet.nebula.protocol.operations

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.*
import io.github.androidpoet.nebula.protocol.engine.FloatExpression

/** Constant float value definition. */
public class DataFloatOperation(
  public val id: Int,
  public val value: Float,
) : Operation(Operations.DATA_FLOAT) {

  override fun applyData(context: RemoteContext) {
    context.setFloat(id, value)
  }

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DATA_FLOAT)
    buffer.writeInt(id)
    buffer.writeFloat(value)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DataFloatOperation {
      return DataFloatOperation(id = buffer.readInt(), value = buffer.readFloat())
    }
  }
}

/** Animated float expression with optional animation spec. */
public class AnimatedFloatOperation(
  public val id: Int,
  public val expression: FloatArray,
  public val animation: FloatArray,
) : Operation(Operations.ANIMATED_FLOAT) {

  public fun toFloatExpression(): FloatExpression {
    return FloatExpression(
      id = id,
      expression = expression,
      animation = if (animation.isNotEmpty()) animation else null,
    )
  }

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.ANIMATED_FLOAT)
    buffer.writeInt(id)
    val packedLength = expression.size or (animation.size shl 16)
    buffer.writeInt(packedLength)
    for (v in expression) buffer.writeFloat(v)
    for (v in animation) buffer.writeFloat(v)
  }

  public companion object {
    public fun read(buffer: WireBuffer): AnimatedFloatOperation {
      val id = buffer.readInt()
      val packedLength = buffer.readInt()
      val exprLen = packedLength and 0xFFFF
      val animLen = (packedLength shr 16) and 0xFFFF
      val expression = FloatArray(exprLen) { buffer.readFloat() }
      val animation = FloatArray(animLen) { buffer.readFloat() }
      return AnimatedFloatOperation(id, expression, animation)
    }
  }
}

/** Text data definition. */
public class DataTextOperation(
  public val id: Int,
  public val text: String,
) : Operation(Operations.DATA_TEXT) {

  override fun applyData(context: RemoteContext) {
    context.loadText(id, text)
  }

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DATA_TEXT)
    buffer.writeInt(id)
    buffer.writeString(text)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DataTextOperation {
      return DataTextOperation(id = buffer.readInt(), text = buffer.readString())
    }
  }
}

/** Integer data definition. */
public class DataIntOperation(
  public val id: Int,
  public val value: Int,
) : Operation(Operations.DATA_INT) {

  override fun applyData(context: RemoteContext) {
    context.loadInt(id, value)
  }

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DATA_INT)
    buffer.writeInt(id)
    buffer.writeInt(value)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DataIntOperation {
      return DataIntOperation(id = buffer.readInt(), value = buffer.readInt())
    }
  }
}

/** Boolean data definition. */
public class DataBooleanOperation(
  public val id: Int,
  public val value: Boolean,
) : Operation(Operations.DATA_BOOLEAN) {

  override fun applyData(context: RemoteContext) {
    context.loadBoolean(id, value)
  }

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DATA_BOOLEAN)
    buffer.writeInt(id)
    buffer.writeBoolean(value)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DataBooleanOperation {
      return DataBooleanOperation(id = buffer.readInt(), value = buffer.readBoolean())
    }
  }
}

/** Long data definition. */
public class DataLongOperation(
  public val id: Int,
  public val value: Long,
) : Operation(Operations.DATA_LONG) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.DATA_LONG)
    buffer.writeInt(id)
    buffer.writeLong(value)
  }

  public companion object {
    public fun read(buffer: WireBuffer): DataLongOperation {
      return DataLongOperation(id = buffer.readInt(), value = buffer.readLong())
    }
  }
}

/** Constant color definition. */
public class ColorConstantOperation(
  public val id: Int,
  public val color: Long,
) : Operation(Operations.COLOR_CONSTANT) {

  override fun applyData(context: RemoteContext) {
    context.loadColor(id, color)
  }

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.COLOR_CONSTANT)
    buffer.writeInt(id)
    buffer.writeColor(color)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ColorConstantOperation {
      return ColorConstantOperation(id = buffer.readInt(), color = buffer.readColor())
    }
  }
}

/** Named variable binding. */
public class NamedVariableOperation(
  public val name: String,
  public val id: Int,
  public val variableType: Int,
) : Operation(Operations.NAMED_VARIABLE) {

  override fun applyData(context: RemoteContext) {
    context.registerNamedVariable(name, id)
  }

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.NAMED_VARIABLE)
    buffer.writeString(name)
    buffer.writeInt(id)
    buffer.writeInt(variableType)
  }

  public companion object {
    public const val TYPE_FLOAT: Int = 0
    public const val TYPE_INT: Int = 1
    public const val TYPE_STRING: Int = 2
    public const val TYPE_BOOLEAN: Int = 3
    public const val TYPE_COLOR: Int = 4

    public fun read(buffer: WireBuffer): NamedVariableOperation {
      return NamedVariableOperation(
        name = buffer.readString(),
        id = buffer.readInt(),
        variableType = buffer.readInt(),
      )
    }
  }
}

/** Float list definition. */
public class FloatListOperation(
  public val id: Int,
  public val values: FloatArray,
) : Operation(Operations.FLOAT_LIST) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.FLOAT_LIST)
    buffer.writeInt(id)
    buffer.writeFloatArray(values)
  }

  public companion object {
    public fun read(buffer: WireBuffer): FloatListOperation {
      return FloatListOperation(id = buffer.readInt(), values = buffer.readFloatArray())
    }
  }
}

/** Color expression with RPN float arrays for ARGB channels. */
public class ColorExpressionOperation(
  public val id: Int,
  public val alphaExpr: FloatArray,
  public val redExpr: FloatArray,
  public val greenExpr: FloatArray,
  public val blueExpr: FloatArray,
) : Operation(Operations.COLOR_EXPRESSIONS) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.COLOR_EXPRESSIONS)
    buffer.writeInt(id)
    buffer.writeFloatArray(alphaExpr)
    buffer.writeFloatArray(redExpr)
    buffer.writeFloatArray(greenExpr)
    buffer.writeFloatArray(blueExpr)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ColorExpressionOperation {
      return ColorExpressionOperation(
        id = buffer.readInt(),
        alphaExpr = buffer.readFloatArray(),
        redExpr = buffer.readFloatArray(),
        greenExpr = buffer.readFloatArray(),
        blueExpr = buffer.readFloatArray(),
      )
    }
  }
}

/** Integer expression with RPN evaluation. */
public class IntegerExpressionOperation(
  public val id: Int,
  public val expression: FloatArray,
) : Operation(Operations.INTEGER_EXPRESSION) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.INTEGER_EXPRESSION)
    buffer.writeInt(id)
    buffer.writeFloatArray(expression)
  }

  public companion object {
    public fun read(buffer: WireBuffer): IntegerExpressionOperation {
      return IntegerExpressionOperation(
        id = buffer.readInt(),
        expression = buffer.readFloatArray(),
      )
    }
  }
}

/** Text constructed from a float value. */
public class TextFromFloatOperation(
  public val id: Int,
  public val sourceId: Int,
  public val format: String,
) : Operation(Operations.TEXT_FROM_FLOAT) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.TEXT_FROM_FLOAT)
    buffer.writeInt(id)
    buffer.writeInt(sourceId)
    buffer.writeString(format)
  }

  public companion object {
    public fun read(buffer: WireBuffer): TextFromFloatOperation {
      return TextFromFloatOperation(
        id = buffer.readInt(),
        sourceId = buffer.readInt(),
        format = buffer.readString(),
      )
    }
  }
}

/** Merge multiple text values into one. */
public class TextMergeOperation(
  public val id: Int,
  public val sourceIds: IntArray,
) : Operation(Operations.TEXT_MERGE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.TEXT_MERGE)
    buffer.writeInt(id)
    buffer.writeInt(sourceIds.size)
    for (sid in sourceIds) buffer.writeInt(sid)
  }

  public companion object {
    public fun read(buffer: WireBuffer): TextMergeOperation {
      val id = buffer.readInt()
      val count = buffer.readInt()
      val sourceIds = IntArray(count) { buffer.readInt() }
      return TextMergeOperation(id, sourceIds)
    }
  }
}

/** Touch expression for interactive dynamic values. */
public class TouchExpressionOperation(
  public val id: Int,
  public val startExpr: FloatArray,
  public val endExpr: FloatArray,
  public val easing: Int,
  public val duration: Float,
) : Operation(Operations.TOUCH_EXPRESSION) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.TOUCH_EXPRESSION)
    buffer.writeInt(id)
    buffer.writeFloatArray(startExpr)
    buffer.writeFloatArray(endExpr)
    buffer.writeInt(easing)
    buffer.writeFloat(duration)
  }

  public companion object {
    public fun read(buffer: WireBuffer): TouchExpressionOperation {
      return TouchExpressionOperation(
        id = buffer.readInt(),
        startExpr = buffer.readFloatArray(),
        endExpr = buffer.readFloatArray(),
        easing = buffer.readInt(),
        duration = buffer.readFloat(),
      )
    }
  }
}
