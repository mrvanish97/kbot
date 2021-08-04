package io.github.mrvanish97.kbot.controller.node

sealed interface BotNodeParameterInfo<T : Any> {
  val isRequired: Boolean
}