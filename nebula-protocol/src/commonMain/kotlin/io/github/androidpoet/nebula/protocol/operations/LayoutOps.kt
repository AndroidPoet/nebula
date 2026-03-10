package io.github.androidpoet.nebula.protocol.operations

import io.github.androidpoet.nebula.protocol.WireBuffer
import io.github.androidpoet.nebula.protocol.core.*

/** Root layout container. */
public class LayoutRootOperation(
  public val width: Float,
  public val height: Float,
) : ComponentOperation(Operations.LAYOUT_ROOT) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_ROOT)
    buffer.writeFloat(width)
    buffer.writeFloat(height)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutRootOperation {
      return LayoutRootOperation(width = buffer.readFloat(), height = buffer.readFloat())
    }
  }
}

/** Box layout (overlay/stack). */
public class LayoutBoxOperation(
  public val contentAlignment: Int = 0,
) : ComponentOperation(Operations.LAYOUT_BOX) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_BOX)
    buffer.writeInt(contentAlignment)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutBoxOperation {
      return LayoutBoxOperation(contentAlignment = buffer.readInt())
    }
  }
}

/** Row layout (horizontal). */
public class LayoutRowOperation(
  public val spacing: Float = 0f,
  public val verticalAlignment: Int = 0,
  public val horizontalArrangement: Int = 0,
) : ComponentOperation(Operations.LAYOUT_ROW) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_ROW)
    buffer.writeFloat(spacing)
    buffer.writeInt(verticalAlignment)
    buffer.writeInt(horizontalArrangement)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutRowOperation {
      return LayoutRowOperation(
        spacing = buffer.readFloat(),
        verticalAlignment = buffer.readInt(),
        horizontalArrangement = buffer.readInt(),
      )
    }
  }
}

/** Column layout (vertical). */
public class LayoutColumnOperation(
  public val spacing: Float = 0f,
  public val horizontalAlignment: Int = 0,
  public val verticalArrangement: Int = 0,
) : ComponentOperation(Operations.LAYOUT_COLUMN) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_COLUMN)
    buffer.writeFloat(spacing)
    buffer.writeInt(horizontalAlignment)
    buffer.writeInt(verticalArrangement)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutColumnOperation {
      return LayoutColumnOperation(
        spacing = buffer.readFloat(),
        horizontalAlignment = buffer.readInt(),
        verticalArrangement = buffer.readInt(),
      )
    }
  }
}

/** Text layout component. */
public class LayoutTextOperation(
  public val textId: Int,
  public val styleId: Int = -1,
  public val maxLines: Int = Int.MAX_VALUE,
  public val overflow: Int = 0,
) : ComponentOperation(Operations.LAYOUT_TEXT) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_TEXT)
    buffer.writeInt(textId)
    buffer.writeInt(styleId)
    buffer.writeInt(maxLines)
    buffer.writeInt(overflow)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutTextOperation {
      return LayoutTextOperation(
        textId = buffer.readInt(),
        styleId = buffer.readInt(),
        maxLines = buffer.readInt(),
        overflow = buffer.readInt(),
      )
    }
  }
}

/** Image layout component. */
public class LayoutImageOperation(
  public val bitmapId: Int,
  public val contentDescription: String = "",
  public val contentScale: Int = 0,
) : ComponentOperation(Operations.LAYOUT_IMAGE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_IMAGE)
    buffer.writeInt(bitmapId)
    buffer.writeString(contentDescription)
    buffer.writeInt(contentScale)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutImageOperation {
      return LayoutImageOperation(
        bitmapId = buffer.readInt(),
        contentDescription = buffer.readString(),
        contentScale = buffer.readInt(),
      )
    }
  }
}

/** Canvas layout for custom drawing. */
public class LayoutCanvasOperation(
  public val width: Float = 0f,
  public val height: Float = 0f,
) : ComponentOperation(Operations.LAYOUT_CANVAS) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_CANVAS)
    buffer.writeFloat(width)
    buffer.writeFloat(height)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutCanvasOperation {
      return LayoutCanvasOperation(width = buffer.readFloat(), height = buffer.readFloat())
    }
  }
}

/** Flow layout (wrapping). */
public class LayoutFlowOperation(
  public val direction: Int = 0,
  public val mainAxisSpacing: Float = 0f,
  public val crossAxisSpacing: Float = 0f,
) : ComponentOperation(Operations.LAYOUT_FLOW) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_FLOW)
    buffer.writeInt(direction)
    buffer.writeFloat(mainAxisSpacing)
    buffer.writeFloat(crossAxisSpacing)
  }

  public companion object {
    public const val HORIZONTAL: Int = 0
    public const val VERTICAL: Int = 1

    public fun read(buffer: WireBuffer): LayoutFlowOperation {
      return LayoutFlowOperation(
        direction = buffer.readInt(),
        mainAxisSpacing = buffer.readFloat(),
        crossAxisSpacing = buffer.readFloat(),
      )
    }
  }
}

/** State layout for conditional rendering based on variable values. */
public class LayoutStateOperation(
  public val variableId: Int,
) : ComponentOperation(Operations.LAYOUT_STATE) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_STATE)
    buffer.writeInt(variableId)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutStateOperation {
      return LayoutStateOperation(variableId = buffer.readInt())
    }
  }
}

/** Content placeholder in a layout. */
public class LayoutContentOperation : ComponentOperation(Operations.LAYOUT_CONTENT) {
  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_CONTENT)
  }
}

/** Collapsible row (shrinks children by priority when space is limited). */
public class LayoutCollapsibleRowOperation(
  public val spacing: Float = 0f,
) : ComponentOperation(Operations.LAYOUT_COLLAPSIBLE_ROW) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_COLLAPSIBLE_ROW)
    buffer.writeFloat(spacing)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutCollapsibleRowOperation {
      return LayoutCollapsibleRowOperation(spacing = buffer.readFloat())
    }
  }
}

/** Collapsible column. */
public class LayoutCollapsibleColumnOperation(
  public val spacing: Float = 0f,
) : ComponentOperation(Operations.LAYOUT_COLLAPSIBLE_COLUMN) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_COLLAPSIBLE_COLUMN)
    buffer.writeFloat(spacing)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutCollapsibleColumnOperation {
      return LayoutCollapsibleColumnOperation(spacing = buffer.readFloat())
    }
  }
}

/** Fit box layout (auto-scales content to fit). */
public class LayoutFitBoxOperation(
  public val contentAlignment: Int = 0,
) : ComponentOperation(Operations.LAYOUT_FIT_BOX) {

  override val isContainer: Boolean = true

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.LAYOUT_FIT_BOX)
    buffer.writeInt(contentAlignment)
  }

  public companion object {
    public fun read(buffer: WireBuffer): LayoutFitBoxOperation {
      return LayoutFitBoxOperation(contentAlignment = buffer.readInt())
    }
  }
}

/** Text style definition. */
public class TextStyleOperation(
  public val styleId: Int,
  public val fontSize: Float,
  public val fontWeight: Int,
  public val color: Long,
  public val letterSpacing: Float,
  public val lineHeight: Float,
  public val textAlign: Int,
  public val italic: Boolean,
  public val decoration: Int,
) : Operation(Operations.TEXT_STYLE) {

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.TEXT_STYLE)
    buffer.writeInt(styleId)
    buffer.writeFloat(fontSize)
    buffer.writeInt(fontWeight)
    buffer.writeColor(color)
    buffer.writeFloat(letterSpacing)
    buffer.writeFloat(lineHeight)
    buffer.writeInt(textAlign)
    buffer.writeBoolean(italic)
    buffer.writeInt(decoration)
  }

  public companion object {
    public fun read(buffer: WireBuffer): TextStyleOperation {
      return TextStyleOperation(
        styleId = buffer.readInt(),
        fontSize = buffer.readFloat(),
        fontWeight = buffer.readInt(),
        color = buffer.readColor(),
        letterSpacing = buffer.readFloat(),
        lineHeight = buffer.readFloat(),
        textAlign = buffer.readInt(),
        italic = buffer.readBoolean(),
        decoration = buffer.readInt(),
      )
    }
  }
}

/** Core text definition (inline text content). */
public class CoreTextOperation(
  public val id: Int,
  public val text: String,
) : Operation(Operations.CORE_TEXT) {

  override fun applyData(context: RemoteContext) {
    context.loadText(id, text)
  }

  override fun write(buffer: WireBuffer) {
    buffer.start(Operations.CORE_TEXT)
    buffer.writeInt(id)
    buffer.writeString(text)
  }

  public companion object {
    public fun read(buffer: WireBuffer): CoreTextOperation {
      return CoreTextOperation(id = buffer.readInt(), text = buffer.readString())
    }
  }
}
