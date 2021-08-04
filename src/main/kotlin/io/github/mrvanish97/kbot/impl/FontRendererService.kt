package io.github.mrvanish97.kbot.impl

import java.io.InputStream

interface FontRendererService {

  @Throws(TextIsBlankException::class, NoSingleFontFoundException::class)
  fun buildImages(message: String): List<InputStream>

}

object TextIsBlankException : Exception("Given text contains no text data to render")

class NoSingleFontFoundException(private val userText: String) :
  Exception("No single acceptable font found for $userText")