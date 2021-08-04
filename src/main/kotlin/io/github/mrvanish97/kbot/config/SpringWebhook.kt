package io.github.mrvanish97.kbot.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.generics.Webhook
import org.telegram.telegrambots.meta.generics.WebhookBot

@Configuration
class SpringWebhook @Autowired constructor(
  private val webhookRegistry: WebhookRegistry
) : Webhook {

  override fun startServer() {
    // Server is already running, so there's no need to start it explicitly
  }

  override fun registerWebhook(callback: WebhookBot) {
    webhookRegistry.register(callback)
  }

  override fun setInternalUrl(internalUrl: String) {
    // Server is already running, so there's no way to change it in the runtime
    // Consider defining it in Spring boot properties
  }

  override fun setKeyStore(keyStore: String?, keyStorePassword: String?) {
    // Server is already running, so there's no way to change it in the runtime
    // Consider defining it in Spring boot properties
  }

}