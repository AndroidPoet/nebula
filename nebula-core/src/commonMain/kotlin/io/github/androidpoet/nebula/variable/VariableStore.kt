package io.github.androidpoet.nebula.variable

import androidx.compose.runtime.mutableStateMapOf
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * Reactive variable store. Compose recomposes automatically
 * when values change, so {{variables}} in text update live.
 */
public class VariableStore {

  private val store = mutableStateMapOf<String, JsonElement>()

  public fun set(key: String, value: JsonElement) {
    store[key] = value
  }

  public fun set(key: String, value: String) {
    store[key] = JsonPrimitive(value)
  }

  public fun set(key: String, value: Number) {
    store[key] = JsonPrimitive(value)
  }

  public fun set(key: String, value: Boolean) {
    store[key] = JsonPrimitive(value)
  }

  public fun get(key: String): JsonElement? = store[key]

  public fun getString(key: String): String? =
    (store[key] as? JsonPrimitive)?.contentOrNull

  public fun getDouble(key: String): Double? =
    (store[key] as? JsonPrimitive)?.doubleOrNull

  public fun getBoolean(key: String): Boolean? =
    (store[key] as? JsonPrimitive)?.booleanOrNull

  public fun isTruthy(key: String): Boolean {
    val element = store[key] ?: return false
    if (element is JsonPrimitive) {
      element.booleanOrNull?.let { return it }
      element.doubleOrNull?.let { return it != 0.0 }
      element.contentOrNull?.let { return it.isNotBlank() }
    }
    return true
  }

  public fun setAll(values: Map<String, JsonElement>) {
    values.forEach { (k, v) -> store[k] = v }
  }

  public fun remove(key: String) {
    store.remove(key)
  }

  public fun clear() {
    store.clear()
  }

  public fun snapshot(): Map<String, JsonElement> = store.toMap()
}
