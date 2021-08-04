package io.github.mrvanish97.kbot.impl

import io.github.mrvanish97.kbot.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.media.InputMedia
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO

@Controller
class ConvertToFontImages @Autowired constructor(
  private val properties: ConvertToImageProperties,
  private val fontsRepository: FontsRepository
) : AbstractTextMessageUpdateHandler() {

  private fun sendNoFontsMessage(param: TextMessage): List<SendMessage> {
    return listOf(
      SendMessage.builder()
        .text("${properties.noFontsMessage} ${param.text}")
        .chatId(param.chatId.toString())
        .replyToMessageId(param.messageId)
        .build()
    )
  }

  override fun handleUpdate(param: TextMessage, update: Update): List<PartialBotApiMethod<*>> {
    val text = param.text.trim()

    val fontList = if (text.containsCyrillic()) {
      fontsRepository.cyrillicFonts
    } else {
      fontsRepository.latinFonts
    }
    if (fontList.isEmpty()) {
      return sendNoFontsMessage(param)
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
    }?.toPair() ?: return sendNoFontsMessage(param)

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

    return if (images.size < MIN_NUMBER_OF_ELEMENTS_IN_MEDIA_GROUP) {
      listOf(
        SendPhoto.builder()
          .chatId(param.chatId.toString())
          .replyToMessageId(param.messageId)
          .photo(toInputFile(images[0]))
          .build()
      )
    } else {
      images.mapIndexed { index, inputStream ->
        Pair(index / MAX_NUMBER_OF_ELEMENTS_IN_MEDIA_GROUP, inputStream)
      }.toMultiMap().map {
        SendMediaGroup.builder()
          .chatId(param.chatId.toString())
          .replyToMessageId(param.messageId)
          .medias(it.value.map { inputStream -> toInputMedia(inputStream) })
          .build()
      }
    }
  }

  override fun nextUpdateHandlerNames(param: TextMessage, update: Update): List<String> {
    TODO("Not yet implemented")
  }
}

private const val FILE_PREFIX = "fonts-"

private fun generateName() = "$FILE_PREFIX${UUID.randomUUID()}"

private fun toInputFile(inputStream: InputStream): InputFile {
  return InputFile(inputStream, generateName())
}

private fun toInputMedia(inputStream: InputStream): InputMedia {
  return InputMediaPhoto().apply {
    setMedia(inputStream, generateName())
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