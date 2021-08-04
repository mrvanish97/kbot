package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.controller.node.BotNodeExecutionPredicate
import io.github.mrvanish97.kbot.controller.node.BotNodeExecutionPredicateFactory
import io.github.mrvanish97.kbot.controller.node.BotNodeMethodDescriptor
import org.springframework.core.ResolvableType
import org.springframework.stereotype.Component

@Component
class ExecuteIfAspectProcessor :
  AbstractBotNodeAspectProcessor<BotNodeMethodDescriptor, BotNodeExecutionPredicateFactory>(
    ResolvableType.forType(Boolean::class.java)
  ) {

  override fun buildFactory(converter: (BotNodeMethodDescriptor) -> Any): BotNodeExecutionPredicateFactory {
    return BotNodeExecutionPredicateFactory {
      val value = converter(it)
      if (value is BotNodeExecutionPredicate) {
        value
      } else {
        BotNodeExecutionPredicate {
          value == true
        }
      }
    }
  }

  override val beanResolvableType = ResolvableType.forType(BotNodeExecutionPredicate::class.java)

  override val beanFactoryResolvableType = ResolvableType.forType(BotNodeExecutionPredicateFactory::class.java)

}