package io.github.mrvanish97.kbot.controller

import io.github.mrvanish97.kbot.anyMessage
import org.telegram.telegrambots.meta.api.objects.Update

enum class TextMessageOrigin {
  MESSAGE,
  EDITED_MESSAGE,
  CHANNEL_POST,
  EDITED_CHANNEL_POST
}

class TextMessage(val update: Update) {

  private val messageToUse = update.anyMessage

  val text: String = update.anyMessage?.text ?: throw IllegalArgumentException("Update doesn't contain any text message")

  val isPlainMessage = update.message != null

  val isEditedMessage = update.editedMessage != null

  val isChannelPost = update.channelPost != null

  val isEditedChannelPost = update.editedMessage != null

  val message = update.anyMessage ?: throw IllegalArgumentException("Update doesn't contain any message")

}