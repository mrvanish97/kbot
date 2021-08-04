package io.github.mrvanish97.kbot.impl

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

@Controller
class UpdateStrategyImpl @Autowired constructor(private val updateHandlers: List<UpdateHandler<*>>) {

  fun answer(update: Update): List<PartialBotApiMethod<*>> {
    @Suppress("UNCHECKED_CAST")
    for (updateHandler in (updateHandlers as List<UpdateHandler<in Any>>)) {
      updateHandler.convertUpdate(update)?.let {
        return updateHandler.handleUpdate(it, update)
      }
    }
    return emptyList()
  }

}