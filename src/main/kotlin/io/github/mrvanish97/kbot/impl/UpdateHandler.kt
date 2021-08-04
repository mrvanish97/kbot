package io.github.mrvanish97.kbot.impl

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

interface UpdateHandler<P> {

  fun handleUpdate(param: P, update: Update) : List<PartialBotApiMethod<*>>

  fun convertUpdate(update: Update): P?

  fun nextUpdateHandlerNames(param: P, update: Update): List<String>

}