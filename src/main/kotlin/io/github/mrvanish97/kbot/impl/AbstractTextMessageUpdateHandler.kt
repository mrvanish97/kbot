package io.github.mrvanish97.kbot.impl

import org.telegram.telegrambots.meta.api.objects.Update

abstract class AbstractTextMessageUpdateHandler : UpdateHandler<TextMessage> {

  override fun convertUpdate(update: Update): TextMessage? {
    return update.message?.let { TextMessage(it.text, it.chatId, it.messageId) }
  }

}