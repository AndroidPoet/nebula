package io.github.androidpoet.nebula

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.androidpoet.nebula.components.NebulaAction
import io.github.androidpoet.nebula.components.NebulaActionHandler
import io.github.androidpoet.nebula.components.NebulaComponent
import io.github.androidpoet.nebula.renderer.NebulaRegistry
import io.github.androidpoet.nebula.renderer.RenderComponent
import io.github.androidpoet.nebula.variable.VariableStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Nebula — Server-driven native UI for Kotlin Multiplatform.
 *
 * Render a component tree from JSON as native Compose UI:
 * ```kotlin
 * Nebula(json = serverResponse) { action ->
 *   when (action) {
 *     is NebulaAction.Custom -> handleCustomAction(action)
 *     else -> {}
 *   }
 * }
 * ```
 *
 * Or render from a pre-parsed component:
 * ```kotlin
 * Nebula(component = parsedComponent, onAction = { ... })
 * ```
 */
@Composable
public fun Nebula(
  json: String,
  variables: Map<String, String> = emptyMap(),
  registry: NebulaRegistry = remember { NebulaRegistry() },
  imageLoader: (@Composable (url: String, contentDescription: String?, modifier: Modifier) -> Unit)? = null,
  onAction: ((NebulaAction) -> Unit)? = null,
) {
  val store = remember { VariableStore() }
  val component = remember(json) { NebulaJson.decodeFromString<NebulaComponent>(json) }

  // Populate initial variables
  remember(variables) {
    variables.forEach { (k, v) -> store.set(k, v) }
    true
  }

  RenderComponent(
    component = component,
    variables = store,
    actionHandler = onAction?.let { NebulaActionHandler(it) },
    registry = registry,
    imageLoader = imageLoader,
  )
}

/**
 * Render a pre-parsed [NebulaComponent] tree.
 */
@Composable
public fun Nebula(
  component: NebulaComponent,
  variables: VariableStore = remember { VariableStore() },
  registry: NebulaRegistry = remember { NebulaRegistry() },
  imageLoader: (@Composable (url: String, contentDescription: String?, modifier: Modifier) -> Unit)? = null,
  onAction: ((NebulaAction) -> Unit)? = null,
) {
  RenderComponent(
    component = component,
    variables = variables,
    actionHandler = onAction?.let { NebulaActionHandler(it) },
    registry = registry,
    imageLoader = imageLoader,
  )
}

/**
 * Parse a JSON string into a [NebulaComponent] tree.
 */
public fun parseNebulaJson(json: String): NebulaComponent {
  return NebulaJson.decodeFromString<NebulaComponent>(json)
}

/**
 * Pre-configured Json instance for Nebula serialization.
 */
public val NebulaJson: Json = Json {
  ignoreUnknownKeys = true
  isLenient = true
  coerceInputValues = true
  classDiscriminator = "type"
}
