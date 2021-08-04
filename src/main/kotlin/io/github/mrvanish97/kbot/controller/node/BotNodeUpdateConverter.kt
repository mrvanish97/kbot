package io.github.mrvanish97.kbot.controller.node

import org.telegram.telegrambots.meta.api.objects.Update

fun interface BotNodeUpdateConverter<T : Any> : BotNodeContextConverter<T> {
  fun convertUpdate(update: Update): T?
  override fun convertContext(context: BotNodeContext): T? {
    return convertUpdate(context.update)
  }
}