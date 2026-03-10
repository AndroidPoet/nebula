package io.github.androidpoet.nebula.protocol.operations

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.*

/** Host action triggered by ID. */
public class HostActionOperation(
  public val actionId: Int,
  public val metadata: String = "",
) : Operation(Operations.HOST_ACTION) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.HOST_ACTION)
    buffer.writeInt(actionId)
    buffer.writeString(metadata)
  }

  public companion object {
    public fun read(buffer: WireBuffer): HostActionOperation {
      return HostActionOperation(
        actionId = buffer.readInt(),
        metadata = buffer.readString(),
      )
    }
  }
}

/** Named host action triggered by string name. */
public class HostNamedActionOperation(
  public val name: String,
  public val metadata: String = "",
) : Operation(Operations.HOST_NAMED_ACTION) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.HOST_NAMED_ACTION)
    buffer.writeString(name)
    buffer.writeString(metadata)
  }

  public companion object {
    public fun read(buffer: WireBuffer): HostNamedActionOperation {
      return HostNamedActionOperation(
        name = buffer.readString(),
        metadata = buffer.readString(),
      )
    }
  }
}

/** Action that changes an integer variable. */
public class ValueIntegerChangeActionOperation(
  public val variableId: Int,
  public val value: Int,
) : Operation(Operations.VALUE_INTEGER_CHANGE_ACTION) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.VALUE_INTEGER_CHANGE_ACTION)
    buffer.writeInt(variableId)
    buffer.writeInt(value)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ValueIntegerChangeActionOperation {
      return ValueIntegerChangeActionOperation(
        variableId = buffer.readInt(),
        value = buffer.readInt(),
      )
    }
  }
}

/** Action that changes a float variable. */
public class ValueFloatChangeActionOperation(
  public val variableId: Int,
  public val value: Float,
) : Operation(Operations.VALUE_FLOAT_CHANGE_ACTION) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.VALUE_FLOAT_CHANGE_ACTION)
    buffer.writeInt(variableId)
    buffer.writeFloat(value)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ValueFloatChangeActionOperation {
      return ValueFloatChangeActionOperation(
        variableId = buffer.readInt(),
        value = buffer.readFloat(),
      )
    }
  }
}

/** Run an action by reference ID. */
public class RunActionOperation(
  public val actionId: Int,
) : Operation(Operations.RUN_ACTION) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.RUN_ACTION)
    buffer.writeInt(actionId)
  }

  public companion object {
    public fun read(buffer: WireBuffer): RunActionOperation {
      return RunActionOperation(actionId = buffer.readInt())
    }
  }
}

/** Conditional operation — executes children only if condition is true. */
public class ConditionalOperation(
  public val variableId: Int,
  public val compareValue: Int,
  public val compareOp: Int,
  public val operationData: ByteArray,
) : Operation(Operations.CONDITIONAL_OPERATIONS) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.CONDITIONAL_OPERATIONS)
    buffer.writeInt(variableId)
    buffer.writeInt(compareValue)
    buffer.writeInt(compareOp)
    buffer.writeBytes(operationData)
  }

  public companion object {
    public const val EQ: Int = 0
    public const val NE: Int = 1
    public const val LT: Int = 2
    public const val GT: Int = 3
    public const val LE: Int = 4
    public const val GE: Int = 5

    public fun read(buffer: WireBuffer): ConditionalOperation {
      return ConditionalOperation(
        variableId = buffer.readInt(),
        compareValue = buffer.readInt(),
        compareOp = buffer.readInt(),
        operationData = buffer.readBytes(),
      )
    }
  }
}

/** Loop start operation — repeats children N times. */
public class LoopStartOperation(
  public val count: Int,
  public val variableId: Int,
) : Operation(Operations.LOOP_START) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LOOP_START)
    buffer.writeInt(count)
    buffer.writeInt(variableId)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LoopStartOperation {
      return LoopStartOperation(count = buffer.readInt(), variableId = buffer.readInt())
    }
  }
}

/** Skip operation — jump ahead by N bytes. */
public class SkipOperation(
  public val byteCount: Int,
) : Operation(Operations.SKIP) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.SKIP)
    buffer.writeInt(byteCount)
  }

  public companion object {
    public fun read(buffer: WireBuffer): SkipOperation {
      return SkipOperation(byteCount = buffer.readInt())
    }
  }
}

/** Particle system definition. */
public class ParticleDefineOperation(
  public val id: Int,
  public val particleData: ByteArray,
) : Operation(Operations.PARTICLE_DEFINE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.PARTICLE_DEFINE)
    buffer.writeInt(id)
    buffer.writeBytes(particleData)
  }

  public companion object {
    public fun read(buffer: WireBuffer): ParticleDefineOperation {
      return ParticleDefineOperation(id = buffer.readInt(), particleData = buffer.readBytes())
    }
  }
}
