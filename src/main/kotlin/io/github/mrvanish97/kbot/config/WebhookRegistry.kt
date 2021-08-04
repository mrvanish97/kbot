package io.github.mrvanish97.kbot.config

import io.github.mrvanish97.kbot.utils.Cleanable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.generics.WebhookBot
import java.util.concurrent.ConcurrentHashMap

private const val DEFAULT_TOKEN = "none"

@Service
class WebhookRegistry @Autowired constructor(
  @Qualifier(PASSWORD_ENCODER_BEAN_NAME) private val passwordEncoder: PasswordEncoder
) {

  companion object {
    const val PASSWORD_ENCODER_BEAN_NAME = "botRegistryPasswordEncoder"
  }

  private val botMap = ConcurrentHashMap<String, Pair<WebhookBot, String>>()

  fun register(bot: WebhookBot) {
    val webhookPathToken = if (bot is WebhookPathTokenProvider) {
      bot.pathToken
    } else {
      DEFAULT_TOKEN
    }
    val encodedPathToken = passwordEncoder.encode(webhookPathToken)
    if (webhookPathToken is Cleanable) {
      webhookPathToken.clean()
    }
    val botName = if (bot is BotNameProvider) {
      bot.botName
    } else {
      bot.botUsername
    }
    botMap[botName] = Pair(bot, encodedPathToken)
  }

  fun getBotByName(botName: String): WebhookBot? {
    val auth = SecurityContextHolder.getContext().authentication
    return if (auth != null && auth !is AnonymousAuthenticationToken) {
      botMap[botName]?.first
    } else {
      null
    }
  }

  fun getBotByNameAndToken(botName: String, token: CharSequence): WebhookBot? {
    val (bot, encodedPathToken) = botMap[botName] ?: return null
    return if (passwordEncoder.matches(token, encodedPathToken)) {
      bot
    } else {
      null
    }
  }

}