@file:JvmName("TelegramBotApiExt")

package io.github.mrvanish97.kbot

import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto
import org.telegram.telegrambots.meta.api.methods.send.*
import org.telegram.telegrambots.meta.api.methods.stickers.AddStickerToSet
import org.telegram.telegrambots.meta.api.methods.stickers.CreateNewStickerSet
import org.telegram.telegrambots.meta.api.methods.stickers.UploadStickerFile
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import java.io.Serializable

sealed interface ExecuteResult

class SerializableResult<T : Serializable>(val result: T) : ExecuteResult

class MessageResult(val message: Message) : ExecuteResult

class ListOfMessagesResult(val messages: List<Message>) : ExecuteResult

object NullExecuteResult : ExecuteResult

fun AbsSender.execute(method: PartialBotApiMethod<*>): ExecuteResult {
  var isMessageList = false
  val result: Any = when (method) {
    is BotApiMethod<*> -> {
      @Suppress("UNCHECKED_CAST")
      execute(method as BotApiMethod<Serializable>)
    }
    is SendVideo -> {
      execute(method)
    }
    is SendAudio -> {
      execute(method)
    }
    is UploadStickerFile -> {
      execute(method)
    }
    is SendSticker -> {
      execute(method)
    }
    is SendPhoto -> {
      execute(method)
    }
    is AddStickerToSet -> {
      execute(method)
    }
    is SendMediaGroup -> {
      isMessageList = true
      execute(method)
    }
    is SetChatPhoto -> {
      execute(method)
    }
    is SendVideoNote -> {
      execute(method)
    }
    is SendVoice -> {
      execute(method)
    }
    is SendAnimation -> {
      execute(method)
    }
    is EditMessageMedia -> {
      execute(method)
    }
    is CreateNewStickerSet -> {
      execute(method)
    }
    is SendDocument -> {
      execute(method)
    }
    else -> {
      throw IllegalArgumentException("Unsupported bot method ${method::class.java}")
    }
  }
  return if (isMessageList) {
    @Suppress("UNCHECKED_CAST")
    (ListOfMessagesResult(result as List<Message>))
  } else {
    when (result) {
      is Message -> MessageResult(result)
      is Serializable -> SerializableResult(result)
      else -> NullExecuteResult
    }
  }
}

val Update.anyMessage: Message?
  get() = message
    ?: editedMessage
    ?: channelPost
    ?: editedChannelPost

val Update.user
  get() = message?.from
    ?: editedMessage?.from
    ?: inlineQuery?.from
    ?: chosenInlineQuery?.from
    ?: callbackQuery?.from
    ?: channelPost?.from
    ?: editedChannelPost?.from
    ?: pollAnswer?.user
    ?: chatMember?.from
    ?: myChatMember?.from