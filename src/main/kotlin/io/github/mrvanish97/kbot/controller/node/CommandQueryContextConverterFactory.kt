package io.github.mrvanish97.kbot.controller.node

import io.github.mrvanish97.kbot.anyMessage
import io.github.mrvanish97.kbot.controller.annotations.Command
import io.github.mrvanish97.kbot.controller.annotations.commandNameToMessage
import io.github.mrvanish97.kbot.controller.annotations.getCommandName
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update

@Component
class CommandQueryContextConverterFactory : BotNodeContextConverterFactory<String> {

  private val stringType = ResolvableType.forType(String::class.java)

  private inner class CommandQueryContextConverter(
    private val commandName: String
  ) : BotNodeUpdateConverter<String> {
    override fun convertUpdate(update: Update): String? {
      return update.anyMessage?.text?.substringAfter("${commandNameToMessage(commandName)} ", "")
    }
    override fun getResolvableType(): ResolvableType {
      return stringType
    }
  }

  override fun build(parameter: BotNodeParameterDescriptor): BotNodeContextConverter<String> {
    val commandName = getCommandName(parameter.method)
    return CommandQueryContextConverter(commandName)
  }
}