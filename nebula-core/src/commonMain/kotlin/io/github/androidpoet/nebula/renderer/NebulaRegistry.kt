package io.github.androidpoet.nebula.renderer

import androidx.compose.runtime.Composable
import io.github.androidpoet.nebula.components.NebulaComponent

/**
 * Registry for custom component renderers.
 * Register your own composables for [NebulaComponent.Custom] types.
 */
public class NebulaRegistry {

  private val renderers = mutableMapOf<String, CustomRenderer>()

  /** Register a renderer for a custom component type. */
  public fun register(type: String, renderer: CustomRenderer) {
    renderers[type] = renderer
  }

  /** Get the renderer for a custom type, or null. */
  internal fun get(type: String): CustomRenderer? = renderers[type]
}

/** Renders a custom component. */
public fun interface CustomRenderer {
  @Composable
  public fun Render(component: NebulaComponent.Custom)
}
