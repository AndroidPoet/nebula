package io.github.androidpoet.nebula.variable

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Resolves {{variable}} placeholders in text content.
 * Supports nested keys like {{user.name}}.
 */
public object VariableResolver {

  private val pattern = Regex("\\{\\{\\s*([a-zA-Z0-9_.]+)\\s*}}")

  public fun resolve(text: String, store: VariableStore): String {
    if (!text.contains("{{")) return text

    return pattern.replace(text) { match ->
      val key = match.groupValues[1]
      val value = store.get(key)
      when {
        value == null -> match.value
        value is JsonPrimitive -> value.contentOrNull ?: match.value
        else -> value.toString()
      }
    }
  }

  public fun hasVariables(text: String): Boolean = pattern.containsMatchIn(text)
}
