package io.github.mrvanish97.kbot.config

interface Bots {

  operator fun get(name: String): BotProperties

  val names: List<String>

}

val Bots.defaultBotName
  get() = names.takeIf { it.size == 1 }?.first()