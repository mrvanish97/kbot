package io.github.mrvanish97.kbot.controller.node

interface BotNodeContextConverterProvider<T : Any> {
  val converter: BotNodeContextConverter<T>
}