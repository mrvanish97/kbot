package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbnsext.annotationBuilder
import io.github.mrvanish97.kbot.controller.node.BotNodeContextConverter
import io.github.mrvanish97.kbot.controller.node.BotNodeContextConverterFactory
import io.github.mrvanish97.kbot.controller.node.BotNodeParameterDescriptor
import org.springframework.core.ResolvableType
import org.springframework.stereotype.Component

@Component
class BotNodeContextConverterAspectProcessor :
  AbstractBotNodeAspectProcessor<BotNodeParameterDescriptor, BotNodeContextConverterFactory<*>>(
    ResolvableType.forType(Any::class.java)
  ) {

  override fun buildFactory(converter: (BotNodeParameterDescriptor) -> Any): BotNodeContextConverterFactory<*> {
    return BotNodeContextConverterFactory {
      val value = converter(it)
      if (value is BotNodeContextConverter<*>) {
        value
      } else {
        BotNodeContextConverter {
          value
        }
      }
    }
  }

  override val beanResolvableType = ResolvableType.forType(BotNodeContextConverter::class.java)

  override val beanFactoryResolvableType = ResolvableType.forType(BotNodeContextConverterFactory::class.java)

}