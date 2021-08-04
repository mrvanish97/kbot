package io.github.mrvanish97.kbot.controller.node

import io.github.mrvanish97.kbot.anyMessage
import io.github.mrvanish97.kbot.controller.annotations.commandNameToMessage
import io.github.mrvanish97.kbot.controller.annotations.getCommandName
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class CommandExecutionPredicateFactory : BotNodeExecutionPredicateFactory {
  override fun build(parameter: BotNodeMethodDescriptor): BotNodeExecutionPredicate {
    val commandName = getCommandName(parameter.method)
    val commandNameMessage = commandNameToMessage(commandName)
    return BotNodeExecutionPredicate {
      val text = it.update.anyMessage?.text
      if (text != null) {
        text.trim() == commandNameMessage || text.startsWith("$commandNameMessage ")
      } else {
        false
      }
    }
  }
}