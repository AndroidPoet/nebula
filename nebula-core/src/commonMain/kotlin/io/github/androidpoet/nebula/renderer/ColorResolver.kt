package io.github.androidpoet.nebula.renderer

import androidx.compose.ui.graphics.Color

/**
 * Resolves color strings to Compose [Color].
 *
 * Supports:
 * - Hex: "#FF5722", "#80FF5722", "#F52"
 * - Named: "red", "blue", "transparent", "white", "black"
 * - Material: "primary", "secondary", "surface", "error" (via theme)
 */
internal object ColorResolver {

  private val namedColors = mapOf(
    "transparent" to Color.Transparent,
    "black" to Color.Black,
    "white" to Color.White,
    "red" to Color(0xFFF44336),
    "pink" to Color(0xFFE91E63),
    "purple" to Color(0xFF9C27B0),
    "deep_purple" to Color(0xFF673AB7),
    "indigo" to Color(0xFF3F51B5),
    "blue" to Color(0xFF2196F3),
    "light_blue" to Color(0xFF03A9F4),
    "cyan" to Color(0xFF00BCD4),
    "teal" to Color(0xFF009688),
    "green" to Color(0xFF4CAF50),
    "light_green" to Color(0xFF8BC34A),
    "lime" to Color(0xFFCDDC39),
    "yellow" to Color(0xFFFFEB3B),
    "amber" to Color(0xFFFFC107),
    "orange" to Color(0xFFFF9800),
    "deep_orange" to Color(0xFFFF5722),
    "brown" to Color(0xFF795548),
    "grey" to Color(0xFF9E9E9E),
    "gray" to Color(0xFF9E9E9E),
    "blue_grey" to Color(0xFF607D8B),
  )

  fun resolve(value: String?): Color? {
    if (value == null) return null

    // Named color
    namedColors[value.lowercase()]?.let { return it }

    // Hex color
    if (value.startsWith("#")) {
      return parseHex(value)
    }

    return null
  }

  fun resolveOrDefault(value: String?, default: Color = Color.Unspecified): Color {
    return resolve(value) ?: default
  }

  private fun parseHex(hex: String): Color? {
    val clean = hex.removePrefix("#")
    return try {
      when (clean.length) {
        3 -> {
          // #RGB → #RRGGBB
          val r = clean[0].toString().repeat(2)
          val g = clean[1].toString().repeat(2)
          val b = clean[2].toString().repeat(2)
          Color(("FF$r$g$b").toLong(16))
        }
        6 -> Color(("FF$clean").toLong(16))
        8 -> Color(clean.toLong(16))
        else -> null
      }
    } catch (_: Exception) {
      null
    }
  }
}
