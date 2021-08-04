package io.github.mrvanish97.kbot.impl

import io.github.mrvanish97.kbot.utils.containsCyrillic
import io.github.mrvanish97.kbot.utils.drawMultilineString
import io.github.mrvanish97.kbot.utils.toMultiMap
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.ArrayList
import javax.imageio.ImageIO

@Service
class FontRendererServiceImpl constructor(
  private val properties: ConvertToImageProperties,
  private val fontsRepository: FontsRepository
) : FontRendererService {

  override fun buildImages(message: String): List<InputStream> {
    val text = message.trim()
    if (text.isBlank()) throw TextIsBlankException

    val fontList = if (text.containsCyrillic()) {
      fontsRepository.cyrillicFonts
    } else {
      fontsRepository.latinFonts
    }
    if (fontList.isEmpty()) {
      throw NoSingleFontFoundException(text)
    }

    val maxNumberOfElements = properties.maxRows * properties.maxColumns
    val pagesAndFonts = fontList.mapIndexed { index, font ->
      Pair(index / maxNumberOfElements, font)
    }.toMultiMap()

    val fontBounds = fontList.associateWith {
      val tempImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
      val tempGraphics = tempImage.createGraphics()
      tempGraphics.applyRenderingHints()
      val bounds = text.split('\n').map { line ->
        tempGraphics.getFontMetrics(it).getStringBounds(line, tempGraphics)
      }.reduce { acc, rectangle2D ->
        Rectangle(
          acc.bounds.width.coerceAtLeast(rectangle2D.bounds.width),
          acc.bounds.height + rectangle2D.bounds.height
        )
      }
      tempGraphics.dispose()
      bounds
    }
    val highestFontWithBounds = fontBounds.maxByOrNull {
      it.value.height
    }?.toPair() ?: throw NoSingleFontFoundException(text)

    val elementHeightWithPadding = properties.maxHeight / properties.maxColumns
    val elementHeight = elementHeightWithPadding - 2 * properties.padding
    val elementWidth = with(highestFontWithBounds.second) {
      (width * elementHeight / height).toInt()
    }
    val lineHeight = let {
      val tempImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
      val tempGraphics = tempImage.createGraphics()
      tempGraphics.applyRenderingHints()
      val fontMetrics = tempGraphics.getFontMetrics(highestFontWithBounds.first)
      val unnoramlizedHeight = fontMetrics.maxAdvance - fontMetrics.height
      unnoramlizedHeight * elementHeight / highestFontWithBounds.second.height
    }.toInt()
    val elementWidthWithPadding = elementWidth + 2 * properties.padding
    val imageWidth = (elementWidth + 2 * properties.padding) * properties.maxColumns
    val imageHeight = properties.maxRows * elementHeightWithPadding

    val images = ArrayList<InputStream>(pagesAndFonts.keys.size)
    for ((_, fonts) in pagesAndFonts) {
      val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB)
      val graphics2D = image.createGraphics()
      graphics2D.color = Color.BLACK
      graphics2D.fillRect(0, 0, imageWidth, imageHeight)
      graphics2D.color = Color.WHITE
      graphics2D.applyRenderingHints()
      for (i in fonts.indices) {
        val x = (i % properties.maxColumns) * elementWidthWithPadding + properties.padding
        val y = (i / properties.maxColumns) * elementHeightWithPadding + properties.padding + lineHeight
        val font = fonts[i]
        val fontMetric = graphics2D.getFontMetrics(font)
        val textLines = text.split('\n')
        val fontWidth = textLines.map {
          fontMetric.getStringBounds(it, graphics2D).width
        }.maxOrNull() ?: 0.0
        val derivedFontSize = (font.size2D * elementWidth / fontWidth).toFloat()
        val sizedFont = font.deriveFont(derivedFontSize)
        graphics2D.font = sizedFont
        graphics2D.drawMultilineString(text, x, y)
      }
      graphics2D.dispose()
      val outputStream = ByteArrayOutputStream()
      ImageIO.write(image, "png", outputStream)
      val inputStream = ByteArrayInputStream(outputStream.toByteArray())
      images.add(inputStream)
    }

    assert(images.isNotEmpty())

    return images
  }

}

private fun Graphics2D.applyRenderingHints() {
  setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
  setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
  setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
  setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE)
  setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
  setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
  setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
  setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
}