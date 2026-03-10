package io.github.androidpoet.nebula.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.androidpoet.nebula.components.Alignment
import io.github.androidpoet.nebula.components.ButtonStyle
import io.github.androidpoet.nebula.components.FontWeight
import io.github.androidpoet.nebula.components.HorizontalAlignment
import io.github.androidpoet.nebula.components.HorizontalArrangement
import io.github.androidpoet.nebula.components.NebulaAction
import io.github.androidpoet.nebula.components.NebulaActionHandler
import io.github.androidpoet.nebula.components.NebulaComponent
import io.github.androidpoet.nebula.components.ProgressType
import io.github.androidpoet.nebula.components.TextAlign
import io.github.androidpoet.nebula.components.TextDecoration
import io.github.androidpoet.nebula.components.VerticalAlignment
import io.github.androidpoet.nebula.components.VerticalArrangement
import io.github.androidpoet.nebula.variable.VariableResolver
import io.github.androidpoet.nebula.variable.VariableStore

/**
 * Renders a [NebulaComponent] tree as native Compose UI.
 *
 * This is the core recursive renderer. It walks the component tree
 * and maps each node to its corresponding Material 3 composable.
 */
@Composable
public fun RenderComponent(
  component: NebulaComponent,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (url: String, contentDescription: String?, modifier: Modifier) -> Unit)? = null,
) {
  val mod = ModifierResolver.resolve(component.modifier, actionHandler)

  when (component) {
    // ── Layout ──────────────────────────────────────
    is NebulaComponent.Column -> RenderColumn(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.Row -> RenderRow(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.Box -> RenderBox(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.LazyColumn -> RenderLazyColumn(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.LazyRow -> RenderLazyRow(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.FlowRow -> RenderFlowRow(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.FlowColumn -> RenderFlowColumn(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.Spacer -> Spacer(modifier = mod)

    // ── Display ─────────────────────────────────────
    is NebulaComponent.Text -> RenderText(component, mod, variables)
    is NebulaComponent.Image -> RenderImage(component, mod, imageLoader)
    is NebulaComponent.Icon -> RenderIcon(component, mod)
    is NebulaComponent.Divider -> RenderDivider(component, mod)
    is NebulaComponent.ProgressIndicator -> RenderProgress(component, mod)
    is NebulaComponent.Badge -> RenderBadge(component, mod, variables, actionHandler, registry, imageLoader)

    // ── Interactive ─────────────────────────────────
    is NebulaComponent.Button -> RenderButton(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.IconButton -> RenderIconButton(component, mod, actionHandler)
    is NebulaComponent.TextField -> RenderTextField(component, mod, variables)
    is NebulaComponent.Checkbox -> RenderCheckbox(component, mod, variables)
    is NebulaComponent.Switch -> RenderSwitch(component, mod, variables)
    is NebulaComponent.Slider -> RenderSlider(component, mod, variables)

    // ── Container ───────────────────────────────────
    is NebulaComponent.Card -> RenderCard(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.Scaffold -> RenderScaffold(component, mod, variables, actionHandler, registry, imageLoader)
    is NebulaComponent.TopAppBar -> RenderTopAppBar(component, mod, variables, actionHandler, registry, imageLoader)

    // ── Conditional ─────────────────────────────────
    is NebulaComponent.Conditional -> {
      val show = variables.isTruthy(component.condition)
      val child = if (show) component.ifTrue else component.ifFalse
      child?.let { RenderComponent(it, variables, actionHandler, registry, imageLoader) }
    }

    // ── Custom ──────────────────────────────────────
    is NebulaComponent.Custom -> {
      registry.get(component.type)?.Render(component)
    }
  }
}

// ── Layout Renderers ───────────────────────────────────

@Composable
private fun RenderColumn(
  component: NebulaComponent.Column,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  Column(
    modifier = modifier,
    verticalArrangement = when (component.verticalArrangement) {
      VerticalArrangement.Top -> if (component.spacing > 0) Arrangement.spacedBy(component.spacing.dp) else Arrangement.Top
      VerticalArrangement.Center -> Arrangement.Center
      VerticalArrangement.Bottom -> Arrangement.Bottom
      VerticalArrangement.SpaceBetween -> Arrangement.SpaceBetween
      VerticalArrangement.SpaceAround -> Arrangement.SpaceAround
      VerticalArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
    },
    horizontalAlignment = component.horizontalAlignment.toCompose(),
  ) {
    component.children.forEach {
      RenderComponent(it, variables, actionHandler, registry, imageLoader)
    }
  }
}

@Composable
private fun RenderRow(
  component: NebulaComponent.Row,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = when (component.horizontalArrangement) {
      HorizontalArrangement.Start -> if (component.spacing > 0) Arrangement.spacedBy(component.spacing.dp) else Arrangement.Start
      HorizontalArrangement.Center -> Arrangement.Center
      HorizontalArrangement.End -> Arrangement.End
      HorizontalArrangement.SpaceBetween -> Arrangement.SpaceBetween
      HorizontalArrangement.SpaceAround -> Arrangement.SpaceAround
      HorizontalArrangement.SpaceEvenly -> Arrangement.SpaceEvenly
    },
    verticalAlignment = component.verticalAlignment.toCompose(),
  ) {
    component.children.forEach {
      RenderComponent(it, variables, actionHandler, registry, imageLoader)
    }
  }
}

@Composable
private fun RenderBox(
  component: NebulaComponent.Box,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  Box(
    modifier = modifier,
    contentAlignment = component.contentAlignment.toCompose(),
  ) {
    component.children.forEach {
      RenderComponent(it, variables, actionHandler, registry, imageLoader)
    }
  }
}

@Composable
private fun RenderLazyColumn(
  component: NebulaComponent.LazyColumn,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  LazyColumn(
    modifier = modifier,
    verticalArrangement = if (component.spacing > 0) Arrangement.spacedBy(component.spacing.dp) else Arrangement.Top,
    horizontalAlignment = component.horizontalAlignment.toCompose(),
  ) {
    items(component.children) { child ->
      RenderComponent(child, variables, actionHandler, registry, imageLoader)
    }
  }
}

@Composable
private fun RenderLazyRow(
  component: NebulaComponent.LazyRow,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  LazyRow(
    modifier = modifier,
    horizontalArrangement = if (component.spacing > 0) Arrangement.spacedBy(component.spacing.dp) else Arrangement.Start,
  ) {
    items(component.children) { child ->
      RenderComponent(child, variables, actionHandler, registry, imageLoader)
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RenderFlowRow(
  component: NebulaComponent.FlowRow,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  androidx.compose.foundation.layout.FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(component.horizontalSpacing.dp),
    verticalArrangement = Arrangement.spacedBy(component.verticalSpacing.dp),
  ) {
    component.children.forEach {
      RenderComponent(it, variables, actionHandler, registry, imageLoader)
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RenderFlowColumn(
  component: NebulaComponent.FlowColumn,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  androidx.compose.foundation.layout.FlowColumn(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(component.horizontalSpacing.dp),
    verticalArrangement = Arrangement.spacedBy(component.verticalSpacing.dp),
  ) {
    component.children.forEach {
      RenderComponent(it, variables, actionHandler, registry, imageLoader)
    }
  }
}

// ── Display Renderers ──────────────────────────────────

@Composable
private fun RenderText(
  component: NebulaComponent.Text,
  modifier: Modifier,
  variables: VariableStore,
) {
  val resolvedText = VariableResolver.resolve(component.content, variables)
  val style = component.style

  // Resolve Material 3 typography role
  val baseTextStyle = when (style?.role) {
    "display_large" -> MaterialTheme.typography.displayLarge
    "display_medium" -> MaterialTheme.typography.displayMedium
    "display_small" -> MaterialTheme.typography.displaySmall
    "headline_large" -> MaterialTheme.typography.headlineLarge
    "headline_medium" -> MaterialTheme.typography.headlineMedium
    "headline_small" -> MaterialTheme.typography.headlineSmall
    "title_large" -> MaterialTheme.typography.titleLarge
    "title_medium" -> MaterialTheme.typography.titleMedium
    "title_small" -> MaterialTheme.typography.titleSmall
    "body_large" -> MaterialTheme.typography.bodyLarge
    "body_medium" -> MaterialTheme.typography.bodyMedium
    "body_small" -> MaterialTheme.typography.bodySmall
    "label_large" -> MaterialTheme.typography.labelLarge
    "label_medium" -> MaterialTheme.typography.labelMedium
    "label_small" -> MaterialTheme.typography.labelSmall
    else -> androidx.compose.ui.text.TextStyle.Default
  }

  val mergedStyle = baseTextStyle.merge(
    androidx.compose.ui.text.TextStyle(
      fontSize = style?.fontSize?.sp ?: baseTextStyle.fontSize,
      fontWeight = style?.fontWeight?.toCompose(),
      color = ColorResolver.resolveOrDefault(style?.color, baseTextStyle.color),
      letterSpacing = style?.letterSpacing?.sp ?: baseTextStyle.letterSpacing,
      lineHeight = style?.lineHeight?.sp ?: baseTextStyle.lineHeight,
      textAlign = style?.textAlign?.toCompose() ?: baseTextStyle.textAlign,
      textDecoration = style?.decoration?.toCompose(),
      fontStyle = if (style?.italic == true) FontStyle.Italic else null,
    ),
  )

  Text(
    text = resolvedText,
    modifier = modifier,
    style = mergedStyle,
    maxLines = component.maxLines,
    overflow = component.overflow.toCompose(),
  )
}

@Composable
private fun RenderImage(
  component: NebulaComponent.Image,
  modifier: Modifier,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  if (imageLoader != null) {
    imageLoader(component.url, component.contentDescription, modifier)
  } else {
    // Placeholder when no image loader is provided
    Box(modifier = modifier) {
      Text("📷", modifier = Modifier)
    }
  }
}

@Composable
private fun RenderIcon(
  component: NebulaComponent.Icon,
  modifier: Modifier,
) {
  val tint = ColorResolver.resolveOrDefault(component.tint, MaterialTheme.colorScheme.onSurface)
  // Use text-based icon rendering for cross-platform compatibility
  Text(
    text = component.name,
    modifier = modifier.size(component.size.dp),
    color = tint,
    fontSize = component.size.sp,
  )
}

@Composable
private fun RenderDivider(
  component: NebulaComponent.Divider,
  modifier: Modifier,
) {
  HorizontalDivider(
    modifier = modifier,
    thickness = component.thickness.dp,
    color = ColorResolver.resolveOrDefault(component.color, MaterialTheme.colorScheme.outlineVariant),
  )
}

@Composable
private fun RenderProgress(
  component: NebulaComponent.ProgressIndicator,
  modifier: Modifier,
) {
  val color = ColorResolver.resolveOrDefault(component.color, MaterialTheme.colorScheme.primary)
  when (component.type) {
    ProgressType.Circular -> {
      if (component.progress != null) {
        CircularProgressIndicator(progress = { component.progress }, modifier = modifier, color = color)
      } else {
        CircularProgressIndicator(modifier = modifier, color = color)
      }
    }
    ProgressType.Linear -> {
      if (component.progress != null) {
        LinearProgressIndicator(progress = { component.progress }, modifier = modifier, color = color)
      } else {
        LinearProgressIndicator(modifier = modifier, color = color)
      }
    }
  }
}

@Composable
private fun RenderBadge(
  component: NebulaComponent.Badge,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  val containerColor = ColorResolver.resolveOrDefault(component.color, MaterialTheme.colorScheme.error)
  if (component.label != null) {
    Badge(
      modifier = modifier,
      containerColor = containerColor,
    ) {
      Text(VariableResolver.resolve(component.label, variables))
    }
  } else {
    Badge(modifier = modifier, containerColor = containerColor)
  }
}

// ── Interactive Renderers ──────────────────────────────

@Composable
private fun RenderButton(
  component: NebulaComponent.Button,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  val onClick = { component.action?.let { actionHandler?.onAction(it) } ?: Unit }
  val content: @Composable () -> Unit = {
    if (component.child != null) {
      RenderComponent(component.child, variables, actionHandler, registry, imageLoader)
    } else if (component.text != null) {
      Text(VariableResolver.resolve(component.text, variables))
    }
  }

  when (component.style) {
    ButtonStyle.Filled -> Button(onClick = onClick, modifier = modifier, content = { content() })
    ButtonStyle.Outlined -> OutlinedButton(onClick = onClick, modifier = modifier, content = { content() })
    ButtonStyle.Elevated -> ElevatedButton(onClick = onClick, modifier = modifier, content = { content() })
    ButtonStyle.Text -> TextButton(onClick = onClick, modifier = modifier, content = { content() })
    ButtonStyle.Tonal -> FilledTonalButton(onClick = onClick, modifier = modifier, content = { content() })
  }
}

@Composable
private fun RenderIconButton(
  component: NebulaComponent.IconButton,
  modifier: Modifier,
  actionHandler: NebulaActionHandler?,
) {
  val tint = ColorResolver.resolveOrDefault(component.tint, MaterialTheme.colorScheme.onSurface)
  IconButton(
    onClick = { component.action?.let { actionHandler?.onAction(it) } },
    modifier = modifier,
  ) {
    Text(
      text = component.icon,
      color = tint,
      fontSize = 24.sp,
    )
  }
}

@Composable
private fun RenderTextField(
  component: NebulaComponent.TextField,
  modifier: Modifier,
  variables: VariableStore,
) {
  var value by remember { mutableStateOf(component.value) }

  OutlinedTextField(
    value = value,
    onValueChange = { newValue ->
      value = newValue
      component.variableKey?.let { variables.set(it, newValue) }
    },
    modifier = modifier,
    label = component.label?.let { { Text(it) } },
    placeholder = component.placeholder?.let { { Text(it) } },
    singleLine = component.singleLine,
  )
}

@Composable
private fun RenderCheckbox(
  component: NebulaComponent.Checkbox,
  modifier: Modifier,
  variables: VariableStore,
) {
  var checked by remember { mutableStateOf(component.checked) }
  Row(modifier = modifier, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
    Checkbox(
      checked = checked,
      onCheckedChange = { newValue ->
        checked = newValue
        component.variableKey?.let { variables.set(it, newValue) }
      },
    )
    component.label?.let { Text(it) }
  }
}

@Composable
private fun RenderSwitch(
  component: NebulaComponent.Switch,
  modifier: Modifier,
  variables: VariableStore,
) {
  var checked by remember { mutableStateOf(component.checked) }
  Row(modifier = modifier, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
    component.label?.let { Text(it, modifier = Modifier.weight(1f)) }
    Switch(
      checked = checked,
      onCheckedChange = { newValue ->
        checked = newValue
        component.variableKey?.let { variables.set(it, newValue) }
      },
    )
  }
}

@Composable
private fun RenderSlider(
  component: NebulaComponent.Slider,
  modifier: Modifier,
  variables: VariableStore,
) {
  var value by remember { mutableStateOf(component.value) }
  Slider(
    value = value,
    onValueChange = { newValue ->
      value = newValue
      component.variableKey?.let { variables.set(it, newValue) }
    },
    modifier = modifier,
    valueRange = component.min..component.max,
    steps = component.steps,
  )
}

// ── Container Renderers ────────────────────────────────

@Composable
private fun RenderCard(
  component: NebulaComponent.Card,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  val shape = resolveShape(component.shape)
  val containerColor = ColorResolver.resolveOrDefault(component.color, MaterialTheme.colorScheme.surfaceContainerLow)

  if (component.action != null) {
    androidx.compose.material3.Card(
      onClick = { actionHandler?.onAction(component.action) },
      modifier = modifier,
      shape = shape,
      elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = component.elevation.dp),
      colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = containerColor),
    ) {
      component.children.forEach {
        RenderComponent(it, variables, actionHandler, registry, imageLoader)
      }
    }
  } else {
    androidx.compose.material3.Card(
      modifier = modifier,
      shape = shape,
      elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = component.elevation.dp),
      colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = containerColor),
    ) {
      component.children.forEach {
        RenderComponent(it, variables, actionHandler, registry, imageLoader)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenderScaffold(
  component: NebulaComponent.Scaffold,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      component.topBar?.let { RenderComponent(it, variables, actionHandler, registry, imageLoader) }
    },
    bottomBar = {
      component.bottomBar?.let { RenderComponent(it, variables, actionHandler, registry, imageLoader) }
    },
    floatingActionButton = {
      component.fab?.let { RenderComponent(it, variables, actionHandler, registry, imageLoader) }
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().then(Modifier.padding(innerPadding))) {
      component.body?.let { RenderComponent(it, variables, actionHandler, registry, imageLoader) }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenderTopAppBar(
  component: NebulaComponent.TopAppBar,
  modifier: Modifier,
  variables: VariableStore,
  actionHandler: NebulaActionHandler?,
  registry: NebulaRegistry,
  imageLoader: (@Composable (String, String?, Modifier) -> Unit)?,
) {
  val containerColor = ColorResolver.resolveOrDefault(component.color, MaterialTheme.colorScheme.surface)
  TopAppBar(
    modifier = modifier,
    title = {
      component.title?.let { RenderComponent(it, variables, actionHandler, registry, imageLoader) }
    },
    navigationIcon = {
      component.navigationIcon?.let { RenderComponent(it, variables, actionHandler, registry, imageLoader) }
    },
    actions = {
      component.actions.forEach { RenderComponent(it, variables, actionHandler, registry, imageLoader) }
    },
    colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
  )
}

// ── Alignment Converters ───────────────────────────────

private fun HorizontalAlignment.toCompose(): androidx.compose.ui.Alignment.Horizontal = when (this) {
  HorizontalAlignment.Start -> androidx.compose.ui.Alignment.Start
  HorizontalAlignment.CenterHorizontally -> androidx.compose.ui.Alignment.CenterHorizontally
  HorizontalAlignment.End -> androidx.compose.ui.Alignment.End
}

private fun VerticalAlignment.toCompose(): androidx.compose.ui.Alignment.Vertical = when (this) {
  VerticalAlignment.Top -> androidx.compose.ui.Alignment.Top
  VerticalAlignment.CenterVertically -> androidx.compose.ui.Alignment.CenterVertically
  VerticalAlignment.Bottom -> androidx.compose.ui.Alignment.Bottom
}

private fun Alignment.toCompose(): androidx.compose.ui.Alignment = when (this) {
  Alignment.TopStart -> androidx.compose.ui.Alignment.TopStart
  Alignment.TopCenter -> androidx.compose.ui.Alignment.TopCenter
  Alignment.TopEnd -> androidx.compose.ui.Alignment.TopEnd
  Alignment.CenterStart -> androidx.compose.ui.Alignment.CenterStart
  Alignment.Center -> androidx.compose.ui.Alignment.Center
  Alignment.CenterEnd -> androidx.compose.ui.Alignment.CenterEnd
  Alignment.BottomStart -> androidx.compose.ui.Alignment.BottomStart
  Alignment.BottomCenter -> androidx.compose.ui.Alignment.BottomCenter
  Alignment.BottomEnd -> androidx.compose.ui.Alignment.BottomEnd
}

private fun FontWeight.toCompose(): androidx.compose.ui.text.font.FontWeight = when (this) {
  FontWeight.Thin -> androidx.compose.ui.text.font.FontWeight.Thin
  FontWeight.ExtraLight -> androidx.compose.ui.text.font.FontWeight.ExtraLight
  FontWeight.Light -> androidx.compose.ui.text.font.FontWeight.Light
  FontWeight.Normal -> androidx.compose.ui.text.font.FontWeight.Normal
  FontWeight.Medium -> androidx.compose.ui.text.font.FontWeight.Medium
  FontWeight.SemiBold -> androidx.compose.ui.text.font.FontWeight.SemiBold
  FontWeight.Bold -> androidx.compose.ui.text.font.FontWeight.Bold
  FontWeight.ExtraBold -> androidx.compose.ui.text.font.FontWeight.ExtraBold
  FontWeight.Black -> androidx.compose.ui.text.font.FontWeight.Black
}

private fun TextAlign.toCompose(): androidx.compose.ui.text.style.TextAlign = when (this) {
  TextAlign.Start -> androidx.compose.ui.text.style.TextAlign.Start
  TextAlign.Center -> androidx.compose.ui.text.style.TextAlign.Center
  TextAlign.End -> androidx.compose.ui.text.style.TextAlign.End
}

private fun TextDecoration.toCompose(): androidx.compose.ui.text.style.TextDecoration = when (this) {
  TextDecoration.None -> androidx.compose.ui.text.style.TextDecoration.None
  TextDecoration.Underline -> androidx.compose.ui.text.style.TextDecoration.Underline
  TextDecoration.LineThrough -> androidx.compose.ui.text.style.TextDecoration.LineThrough
}

private fun io.github.androidpoet.nebula.components.TextOverflow.toCompose(): TextOverflow = when (this) {
  io.github.androidpoet.nebula.components.TextOverflow.Clip -> TextOverflow.Clip
  io.github.androidpoet.nebula.components.TextOverflow.Ellipsis -> TextOverflow.Ellipsis
  io.github.androidpoet.nebula.components.TextOverflow.Visible -> TextOverflow.Visible
}

