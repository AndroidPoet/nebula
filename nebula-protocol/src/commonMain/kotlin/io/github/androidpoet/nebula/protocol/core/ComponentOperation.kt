package io.github.androidpoet.nebula.protocol.core

import io.github.androidpoet.nebula.protocol.WireBuffer

/**
 * Base class for operations that form the component tree.
 *
 * Components can be containers (with children) or leaf nodes.
 * During inflation, container start/end pairs are matched to
 * build parent-child relationships.
 */
public abstract class ComponentOperation(opcode: Int) : Operation(opcode) {

  /** Child components (populated during tree inflation). */
  public val children: MutableList<ComponentOperation> = mutableListOf()

  /** Modifier operations attached to this component. */
  public val modifierOps: MutableList<Operation> = mutableListOf()

  /** Whether this component is a container with children. */
  public open val isContainer: Boolean = false
}

/** Marks the end of a container scope. */
public class ContainerEndOperation : Operation(Operations.CONTAINER_END) {
  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.CONTAINER_END)
  }
}
