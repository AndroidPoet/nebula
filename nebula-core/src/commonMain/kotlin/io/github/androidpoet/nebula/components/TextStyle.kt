package io.github.androidpoet.nebula.components

import kotlinx.serialization.Serializable

@Serializable
public data class TextStyle(
  val fontSize: Float? = null,
  val fontWeight: FontWeight? = null,
  val color: String? = null,
  val letterSpacing: Float? = null,
  val lineHeight: Float? = null,
  val textAlign: TextAlign? = null,
  val decoration: TextDecoration? = null,
  val italic: Boolean? = null,
  /** Material 3 typography role: "display_large", "headline_medium", "body_small", etc. */
  val role: String? = null,
)
