package io.github.mrvanish97.kbot.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.generics.Webhook
import org.telegram.telegrambots.starter.TelegramBotStarterConfiguration
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@Configuration
class SpringBotConfiguration @Autowired constructor(
  private val webhook: Webhook
) : TelegramBotStarterConfiguration() {

  override fun telegramBotsApi(): TelegramBotsApi {
    return TelegramBotsApi(DefaultBotSession::class.java, webhook)
  }

}