package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.controller.node.BotNodeAspectAbstractFactory
import org.springframework.core.ResolvableType

interface BotNodeAspectProcessor<F: BotNodeAspectAbstractFactory<*, *>> {

  val beanResolvableType: ResolvableType

  val beanFactoryResolvableType: ResolvableType

  @Throws(InvalidAbstractFactoryBeanException::class)
  fun processRawFactoryBean(factoryBean: Any): F

}