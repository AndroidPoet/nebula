package io.github.androidpoet.nebula.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Actions that components can fire.
 * The host app handles these via [NebulaActionHandler].
 */
@Serializable
public sealed interface NebulaAction {

  /** Navigate to a route. */
  @Serializable
  @SerialName("navigate")
  public data class Navigate(
    val route: String,
    val popCurrent: Boolean = false,
  ) : NebulaAction

  /** Navigate back. */
  @Serializable
  @SerialName("back")
  public data object Back : NebulaAction

  /** Open a URL in the browser. */
  @Serializable
  @SerialName("open_url")
  public data class OpenUrl(val url: String) : NebulaAction

  /** Set a variable value. */
  @Serializable
  @SerialName("set_value")
  public data class SetValue(
    val key: String,
    val value: JsonElement,
  ) : NebulaAction

  /** Fire a named event the host app can handle. */
  @Serializable
  @SerialName("custom")
  public data class Custom(
    val name: String,
    val data: Map<String, JsonElement> = emptyMap(),
  ) : NebulaAction

  /** Execute multiple actions in sequence. */
  @Serializable
  @SerialName("multi")
  public data class Multi(
    val actions: List<NebulaAction>,
  ) : NebulaAction

  /** Show a snackbar message. */
  @Serializable
  @SerialName("snackbar")
  public data class Snackbar(
    val message: String,
    val actionLabel: String? = null,
    val onAction: NebulaAction? = null,
  ) : NebulaAction
}

/** Callback interface for host apps to handle actions. */
public fun interface NebulaActionHandler {
  public fun onAction(action: NebulaAction)
}
