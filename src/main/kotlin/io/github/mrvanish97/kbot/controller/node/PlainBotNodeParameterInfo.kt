package io.github.mrvanish97.kbot.controller.node

internal data class PlainBotNodeParameterInfo<T : Any>(
  override val converter: BotNodeContextConverter<T>,
  override val isRequired: Boolean
) : BotNodeParameterInfo<T>, BotNodeContextConverterProvider<T>