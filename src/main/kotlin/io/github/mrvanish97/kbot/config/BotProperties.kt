package io.github.mrvanish97.kbot.config

import java.nio.file.Path

interface BotProperties {

  val telegramToken: String

  val username: String

  val url: String

  val botName: String

  val webhookToken: String

  val certificatePath: Path?

  val initialOwnerId: Long

}