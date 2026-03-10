package io.github.androidpoet.nebula.protocol

import io.github.androidpoet.nebula.protocol.core.FloatVariableStore

class TestFloatStore : FloatVariableStore {
  private val vars = mutableMapOf<Int, Float>()

  override fun getFloat(id: Int): Float = vars[id] ?: 0f
  override fun setFloat(id: Int, value: Float) { vars[id] = value }
  override fun hasFloat(id: Int): Boolean = id in vars
}
