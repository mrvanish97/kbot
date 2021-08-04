package io.github.mrvanish97.kbot.impl

data class TextMessage(
  val text: String,
  val chatId: Long,
  val messageId: Int
)