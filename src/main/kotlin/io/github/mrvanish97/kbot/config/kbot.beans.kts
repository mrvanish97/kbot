package io.github.mrvanish97.kbot.config

import io.github.mrvanish97.kbnsext.annotateWith
import io.github.mrvanish97.kbot.SpringWebhookBotEx
import io.github.mrvanish97.kbot.SpringWebhookBotEx.Companion.DEFAULT_FACTORY_BEAN_NAME
import io.github.mrvanish97.kbot.anyMessage
import io.github.mrvanish97.kbot.config.WebhookRegistry.Companion.PASSWORD_ENCODER_BEAN_NAME
import io.github.mrvanish97.kbot.controller.RootBotController.Companion.EXECUTOR_FACTORY_BEAN_NAME
import io.github.mrvanish97.kbot.controller.addSendResultConverter
import io.github.mrvanish97.kbot.controller.addSingeSendResultConverter
import org.springframework.core.convert.converter.ConverterRegistry
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.getProperty
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.concurrent.Executor
import java.util.concurrent.Executors

rootConfiguration().annotateWith<BotsPropertySource>()

bean {
  environment.propertySources
}

bean {
  DefaultBotOptions().apply {
    maxThreads = Runtime.getRuntime().availableProcessors()
  }
}

private val baseExecutorBeanName = "io.github.mrvanish97.kbot.api.config.kbot.beans.baseExecutorBean"

bean(baseExecutorBeanName) {
  Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
}

bean(EXECUTOR_FACTORY_BEAN_NAME) {
  fun(context: SecurityContext): Executor {
    return DelegatingSecurityContextExecutor(
      ref(baseExecutorBeanName),
      context
    )
  }
}

fun onTrueOrNull(propertyName: String): ConfigurableEnvironment.() -> Boolean = {
  getProperty<Boolean>(propertyName) ?: true
}

environment(onTrueOrNull("kbot.use-default-password-encoder")).then {
  bean(name = PASSWORD_ENCODER_BEAN_NAME) {
    SCryptPasswordEncoder()
  }
}

environment(onTrueOrNull("kbot.use-default-path-encoder")).then {
  bean {
    QueryParamBotPathEncoder()
  }
}

bean(DEFAULT_FACTORY_BEAN_NAME) {
  fun(botProperties: BotProperties): SpringWebhookBotEx {
    val setWebhook = SetWebhook.builder()
      .url(botProperties.url)
      .dropPendingUpdates(true)
      .clearAllowedUpdates()
      .build()
    return SpringWebhookBotEx(
      defaultBotOptions = ref(),
      setWebhook = setWebhook,
      botProperties = botProperties,
      encodedBotPath = ref<BotPathEncoder>().encode(botProperties.botName, botProperties.webhookToken),
      applicationContext = context,
      conversionService = ref()
    )
  }
}

init {
  val registry = ref<ConverterRegistry>()
  registry.addSingeSendResultConverter<String> { update, result ->
    val chatId = update.anyMessage?.chatId?.toString()
    if (result.isNotBlank() && chatId != null) {
      SendMessage.builder()
        .chatId(chatId)
        .text(result)
        .build()
    } else {
      null
    }
  }

}