package io.github.androidpoet.nebula.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The component tree. Every UI element the server can send.
 *
 * Compose-native naming — no Flutter vocabulary.
 * Each component maps directly to a Material 3 composable.
 */
@Serializable
public sealed interface NebulaComponent {

  /** The modifier applied to this component. */
  public val modifier: NebulaModifier?
    get() = null

  // ── Layout ────────────────────────────────────────────

  @Serializable
  @SerialName("column")
  public data class Column(
    val children: List<NebulaComponent> = emptyList(),
    val spacing: Float = 0f,
    val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Start,
    val verticalArrangement: VerticalArrangement = VerticalArrangement.Top,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("row")
  public data class Row(
    val children: List<NebulaComponent> = emptyList(),
    val spacing: Float = 0f,
    val verticalAlignment: VerticalAlignment = VerticalAlignment.Top,
    val horizontalArrangement: HorizontalArrangement = HorizontalArrangement.Start,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("box")
  public data class Box(
    val children: List<NebulaComponent> = emptyList(),
    val contentAlignment: Alignment = Alignment.TopStart,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("lazy_column")
  public data class LazyColumn(
    val children: List<NebulaComponent> = emptyList(),
    val spacing: Float = 0f,
    val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Start,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("lazy_row")
  public data class LazyRow(
    val children: List<NebulaComponent> = emptyList(),
    val spacing: Float = 0f,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("flow_row")
  public data class FlowRow(
    val children: List<NebulaComponent> = emptyList(),
    val horizontalSpacing: Float = 0f,
    val verticalSpacing: Float = 0f,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("flow_column")
  public data class FlowColumn(
    val children: List<NebulaComponent> = emptyList(),
    val horizontalSpacing: Float = 0f,
    val verticalSpacing: Float = 0f,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("spacer")
  public data class Spacer(
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  // ── Display ───────────────────────────────────────────

  @Serializable
  @SerialName("text")
  public data class Text(
    val content: String,
    val style: TextStyle? = null,
    val maxLines: Int = Int.MAX_VALUE,
    val overflow: TextOverflow = TextOverflow.Clip,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("image")
  public data class Image(
    val url: String,
    val contentDescription: String? = null,
    val contentScale: ContentScale = ContentScale.Fit,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("icon")
  public data class Icon(
    val name: String,
    val contentDescription: String? = null,
    val tint: String? = null,
    val size: Float = 24f,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("divider")
  public data class Divider(
    val thickness: Float = 1f,
    val color: String? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("progress_indicator")
  public data class ProgressIndicator(
    val type: ProgressType = ProgressType.Circular,
    val progress: Float? = null,
    val color: String? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("badge")
  public data class Badge(
    val label: String? = null,
    val color: String? = null,
    val child: NebulaComponent? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  // ── Interactive ───────────────────────────────────────

  @Serializable
  @SerialName("button")
  public data class Button(
    val child: NebulaComponent? = null,
    val text: String? = null,
    val style: ButtonStyle = ButtonStyle.Filled,
    val action: NebulaAction? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("icon_button")
  public data class IconButton(
    val icon: String,
    val contentDescription: String? = null,
    val tint: String? = null,
    val action: NebulaAction? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("text_field")
  public data class TextField(
    val value: String = "",
    val label: String? = null,
    val placeholder: String? = null,
    val variableKey: String? = null,
    val keyboardType: KeyboardType = KeyboardType.Text,
    val singleLine: Boolean = true,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("checkbox")
  public data class Checkbox(
    val checked: Boolean = false,
    val label: String? = null,
    val variableKey: String? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("switch")
  public data class Switch(
    val checked: Boolean = false,
    val label: String? = null,
    val variableKey: String? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("slider")
  public data class Slider(
    val value: Float = 0f,
    val min: Float = 0f,
    val max: Float = 1f,
    val steps: Int = 0,
    val variableKey: String? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  // ── Container ─────────────────────────────────────────

  @Serializable
  @SerialName("card")
  public data class Card(
    val children: List<NebulaComponent> = emptyList(),
    val elevation: Float = 1f,
    val shape: NebulaShape? = null,
    val color: String? = null,
    val action: NebulaAction? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("scaffold")
  public data class Scaffold(
    val topBar: NebulaComponent? = null,
    val bottomBar: NebulaComponent? = null,
    val fab: NebulaComponent? = null,
    val body: NebulaComponent? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  @Serializable
  @SerialName("top_app_bar")
  public data class TopAppBar(
    val title: NebulaComponent? = null,
    val navigationIcon: NebulaComponent? = null,
    val actions: List<NebulaComponent> = emptyList(),
    val color: String? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  // ── Conditional ───────────────────────────────────────

  @Serializable
  @SerialName("conditional")
  public data class Conditional(
    val condition: String,
    val ifTrue: NebulaComponent? = null,
    val ifFalse: NebulaComponent? = null,
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent

  // ── Custom ────────────────────────────────────────────

  @Serializable
  @SerialName("custom")
  public data class Custom(
    val type: String,
    val properties: Map<String, kotlinx.serialization.json.JsonElement> = emptyMap(),
    val children: List<NebulaComponent> = emptyList(),
    override val modifier: NebulaModifier? = null,
  ) : NebulaComponent
}
