package io.github.mrvanish97.kbot.controller.node

import org.springframework.core.ResolvableType

private val type = ResolvableType.forClass(BotNodeContextConverter::class.java)

fun interface BotNodeContextConverterFactory<T : Any> :
  BotNodeAspectAbstractFactory<BotNodeParameterDescriptor, BotNodeContextConverter<T>> {
  override fun getResolvableType() = type
}