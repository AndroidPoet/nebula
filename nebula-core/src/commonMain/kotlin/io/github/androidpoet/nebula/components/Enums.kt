package io.github.androidpoet.nebula.components

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class HorizontalAlignment {
  @SerialName("start") Start,
  @SerialName("center") CenterHorizontally,
  @SerialName("end") End,
}

@Serializable
public enum class VerticalAlignment {
  @SerialName("top") Top,
  @SerialName("center") CenterVertically,
  @SerialName("bottom") Bottom,
}

@Serializable
public enum class Alignment {
  @SerialName("top_start") TopStart,
  @SerialName("top_center") TopCenter,
  @SerialName("top_end") TopEnd,
  @SerialName("center_start") CenterStart,
  @SerialName("center") Center,
  @SerialName("center_end") CenterEnd,
  @SerialName("bottom_start") BottomStart,
  @SerialName("bottom_center") BottomCenter,
  @SerialName("bottom_end") BottomEnd,
}

@Serializable
public enum class VerticalArrangement {
  @SerialName("top") Top,
  @SerialName("center") Center,
  @SerialName("bottom") Bottom,
  @SerialName("space_between") SpaceBetween,
  @SerialName("space_around") SpaceAround,
  @SerialName("space_evenly") SpaceEvenly,
}

@Serializable
public enum class HorizontalArrangement {
  @SerialName("start") Start,
  @SerialName("center") Center,
  @SerialName("end") End,
  @SerialName("space_between") SpaceBetween,
  @SerialName("space_around") SpaceAround,
  @SerialName("space_evenly") SpaceEvenly,
}

@Serializable
public enum class TextOverflow {
  @SerialName("clip") Clip,
  @SerialName("ellipsis") Ellipsis,
  @SerialName("visible") Visible,
}

@Serializable
public enum class ContentScale {
  @SerialName("fit") Fit,
  @SerialName("fill") Fill,
  @SerialName("crop") Crop,
  @SerialName("inside") Inside,
  @SerialName("none") None,
}

@Serializable
public enum class ButtonStyle {
  @SerialName("filled") Filled,
  @SerialName("outlined") Outlined,
  @SerialName("elevated") Elevated,
  @SerialName("text") Text,
  @SerialName("tonal") Tonal,
}

@Serializable
public enum class ProgressType {
  @SerialName("circular") Circular,
  @SerialName("linear") Linear,
}

@Serializable
public enum class KeyboardType {
  @SerialName("text") Text,
  @SerialName("number") Number,
  @SerialName("email") Email,
  @SerialName("phone") Phone,
  @SerialName("password") Password,
}

@Serializable
public enum class FontWeight {
  @SerialName("thin") Thin,
  @SerialName("extra_light") ExtraLight,
  @SerialName("light") Light,
  @SerialName("normal") Normal,
  @SerialName("medium") Medium,
  @SerialName("semi_bold") SemiBold,
  @SerialName("bold") Bold,
  @SerialName("extra_bold") ExtraBold,
  @SerialName("black") Black,
}

@Serializable
public enum class TextDecoration {
  @SerialName("none") None,
  @SerialName("underline") Underline,
  @SerialName("line_through") LineThrough,
}

@Serializable
public enum class TextAlign {
  @SerialName("start") Start,
  @SerialName("center") Center,
  @SerialName("end") End,
}

@Serializable
public enum class ShapeType {
  @SerialName("rectangle") Rectangle,
  @SerialName("rounded") Rounded,
  @SerialName("circle") Circle,
}
