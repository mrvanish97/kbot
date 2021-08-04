package io.github.mrvanish97.kbot.controller.node

import org.springframework.core.ResolvableTypeProvider

interface BotNodeAspectAbstractFactory<V, T> : ResolvableTypeProvider {

  fun build(parameter: V): T

}