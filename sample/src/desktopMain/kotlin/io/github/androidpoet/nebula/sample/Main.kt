package io.github.androidpoet.nebula.sample

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.androidpoet.nebula.Nebula

fun main() = application {
  val state = rememberWindowState(size = DpSize(420.dp, 800.dp))

  Window(
    onCloseRequest = ::exitApplication,
    title = "Nebula — Server-Driven UI Demo",
    state = state,
  ) {
    MaterialTheme(colorScheme = darkColorScheme()) {
      Surface(modifier = Modifier.fillMaxSize()) {
        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Profile", "Settings", "Form")

        Column {
          TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
              Tab(
                selected = selectedTab == index,
                onClick = { selectedTab = index },
                text = { Text(title) },
              )
            }
          }

          Crossfade(targetState = selectedTab, modifier = Modifier.padding(16.dp)) { tab ->
            when (tab) {
              0 -> Nebula(
                json = profileJson,
                variables = mapOf(
                  "user.name" to "Ranbir Singh",
                  "user.role" to "Android Engineer",
                  "user.projects" to "42",
                  "user.followers" to "1.2k",
                  "user.stars" to "3.5k",
                ),
                onAction = { println("Action: $it") },
              )
              1 -> Nebula(
                json = settingsJson,
                onAction = { println("Action: $it") },
              )
              2 -> Nebula(
                json = formJson,
                onAction = { println("Action: $it") },
              )
            }
          }
        }
      }
    }
  }
}

// Server would send these — hardcoded for demo
private val profileJson = """
{
  "type": "column",
  "spacing": 16,
  "horizontalAlignment": "center",
  "modifier": { "padding": { "all": 16 }, "fillMaxWidth": true },
  "children": [
    {
      "type": "box",
      "modifier": {
        "fillMaxWidth": true,
        "background": "#6750A4",
        "shape": { "type": "rounded", "cornerRadius": 24 },
        "padding": { "all": 24 }
      },
      "contentAlignment": "center",
      "children": [
        {
          "type": "column",
          "horizontalAlignment": "center",
          "spacing": 8,
          "children": [
            {
              "type": "text",
              "content": "✨",
              "style": { "fontSize": 48 }
            },
            {
              "type": "text",
              "content": "{{user.name}}",
              "style": { "fontSize": 24, "fontWeight": "bold", "color": "#FFFFFF" }
            },
            {
              "type": "text",
              "content": "{{user.role}}",
              "style": { "fontSize": 14, "color": "#E0D0FF" }
            }
          ]
        }
      ]
    },
    {
      "type": "row",
      "modifier": { "fillMaxWidth": true },
      "horizontalArrangement": "space_evenly",
      "children": [
        {
          "type": "column",
          "horizontalAlignment": "center",
          "children": [
            { "type": "text", "content": "{{user.projects}}", "style": { "fontSize": 20, "fontWeight": "bold" } },
            { "type": "text", "content": "Projects", "style": { "fontSize": 12, "color": "#888888" } }
          ]
        },
        {
          "type": "column",
          "horizontalAlignment": "center",
          "children": [
            { "type": "text", "content": "{{user.followers}}", "style": { "fontSize": 20, "fontWeight": "bold" } },
            { "type": "text", "content": "Followers", "style": { "fontSize": 12, "color": "#888888" } }
          ]
        },
        {
          "type": "column",
          "horizontalAlignment": "center",
          "children": [
            { "type": "text", "content": "{{user.stars}}", "style": { "fontSize": 20, "fontWeight": "bold" } },
            { "type": "text", "content": "Stars", "style": { "fontSize": 12, "color": "#888888" } }
          ]
        }
      ]
    },
    { "type": "divider" },
    {
      "type": "card",
      "modifier": { "fillMaxWidth": true },
      "elevation": 2,
      "shape": { "type": "rounded", "cornerRadius": 16 },
      "children": [
        {
          "type": "column",
          "modifier": { "padding": { "all": 16 } },
          "spacing": 12,
          "children": [
            { "type": "text", "content": "Recent Projects", "style": { "fontSize": 18, "fontWeight": "semi_bold" } },
            {
              "type": "row",
              "spacing": 12,
              "children": [
                { "type": "text", "content": "🚀", "style": { "fontSize": 20 } },
                {
                  "type": "column",
                  "children": [
                    { "type": "text", "content": "Nebula", "style": { "fontWeight": "medium" } },
                    { "type": "text", "content": "Server-driven UI for KMP", "style": { "fontSize": 12, "color": "#888888" } }
                  ]
                }
              ]
            },
            {
              "type": "row",
              "spacing": 12,
              "children": [
                { "type": "text", "content": "💎", "style": { "fontSize": 20 } },
                {
                  "type": "column",
                  "children": [
                    { "type": "text", "content": "Superwall KMP", "style": { "fontWeight": "medium" } },
                    { "type": "text", "content": "Paywall SDK for Kotlin Multiplatform", "style": { "fontSize": 12, "color": "#888888" } }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "button",
      "text": "View All Projects",
      "style": "filled",
      "modifier": { "fillMaxWidth": true },
      "action": { "type": "custom", "name": "view_projects" }
    }
  ]
}
""".trimIndent()

private val settingsJson = """
{
  "type": "column",
  "spacing": 8,
  "modifier": { "fillMaxWidth": true },
  "children": [
    { "type": "text", "content": "Settings", "style": { "role": "headline_medium", "fontWeight": "bold" } },
    { "type": "spacer", "modifier": { "height": 8 } },
    {
      "type": "card",
      "modifier": { "fillMaxWidth": true },
      "shape": { "type": "rounded", "cornerRadius": 16 },
      "children": [
        {
          "type": "column",
          "modifier": { "padding": { "all": 16 } },
          "spacing": 4,
          "children": [
            { "type": "text", "content": "Appearance", "style": { "fontSize": 14, "color": "#6750A4", "fontWeight": "medium" } },
            { "type": "switch", "label": "Dark Mode", "checked": true, "variableKey": "dark_mode" },
            { "type": "switch", "label": "Compact Layout", "checked": false, "variableKey": "compact" }
          ]
        }
      ]
    },
    {
      "type": "card",
      "modifier": { "fillMaxWidth": true },
      "shape": { "type": "rounded", "cornerRadius": 16 },
      "children": [
        {
          "type": "column",
          "modifier": { "padding": { "all": 16 } },
          "spacing": 4,
          "children": [
            { "type": "text", "content": "Notifications", "style": { "fontSize": 14, "color": "#6750A4", "fontWeight": "medium" } },
            { "type": "switch", "label": "Push Notifications", "checked": true, "variableKey": "push" },
            { "type": "switch", "label": "Email Digest", "checked": false, "variableKey": "email" },
            { "type": "switch", "label": "In-App Alerts", "checked": true, "variableKey": "alerts" }
          ]
        }
      ]
    },
    {
      "type": "card",
      "modifier": { "fillMaxWidth": true },
      "shape": { "type": "rounded", "cornerRadius": 16 },
      "children": [
        {
          "type": "column",
          "modifier": { "padding": { "all": 16 } },
          "spacing": 8,
          "children": [
            { "type": "text", "content": "Font Size", "style": { "fontSize": 14, "color": "#6750A4", "fontWeight": "medium" } },
            { "type": "slider", "value": 0.5, "min": 0, "max": 1, "variableKey": "font_scale" }
          ]
        }
      ]
    },
    { "type": "spacer", "modifier": { "height": 8 } },
    {
      "type": "button",
      "text": "Sign Out",
      "style": "outlined",
      "modifier": { "fillMaxWidth": true },
      "action": { "type": "custom", "name": "sign_out" }
    }
  ]
}
""".trimIndent()

private val formJson = """
{
  "type": "column",
  "spacing": 16,
  "modifier": { "fillMaxWidth": true },
  "children": [
    { "type": "text", "content": "Contact Us", "style": { "role": "headline_medium", "fontWeight": "bold" } },
    { "type": "text", "content": "We'd love to hear from you", "style": { "fontSize": 14, "color": "#888888" } },
    { "type": "spacer", "modifier": { "height": 8 } },
    { "type": "text_field", "label": "Name", "placeholder": "Your name", "variableKey": "form.name" },
    { "type": "text_field", "label": "Email", "placeholder": "your@email.com", "variableKey": "form.email", "keyboardType": "email" },
    { "type": "text_field", "label": "Message", "placeholder": "Tell us what's on your mind...", "variableKey": "form.message", "singleLine": false },
    { "type": "checkbox", "label": "Subscribe to newsletter", "variableKey": "form.subscribe" },
    { "type": "spacer", "modifier": { "height": 8 } },
    {
      "type": "row",
      "spacing": 12,
      "modifier": { "fillMaxWidth": true },
      "horizontalArrangement": "end",
      "children": [
        { "type": "button", "text": "Cancel", "style": "text", "action": { "type": "back" } },
        { "type": "button", "text": "Submit", "style": "filled", "action": { "type": "custom", "name": "submit_form" } }
      ]
    }
  ]
}
""".trimIndent()
