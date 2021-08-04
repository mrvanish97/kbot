package io.github.mrvanish97.kbot.security

import io.github.mrvanish97.kbnsext.javaName
import io.github.mrvanish97.kbot.Bot
import io.github.mrvanish97.kbot.config.Bots
import io.github.mrvanish97.kbot.config.WebhookRegistry
import io.github.mrvanish97.kbot.config.defaultBotName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class TelegramAuthProvider @Autowired constructor(
  private val webhookRegistry: WebhookRegistry,
  botAuthenticationProvidersList: List<BotAuthenticationProvider<out Authentication>>,
  bots: Bots
) : AuthenticationProvider {

  private val botAuthenticationProviders = botAuthenticationProvidersList.asSequence()
    .mapNotNull {
      val botName = AnnotationAttributes.fromMap(
        AnnotationMetadata.introspect(it::class.java).getAnnotationAttributes(Bot::class.javaName)
      )?.getString(Bot::value.name) ?: bots.defaultBotName ?: return@mapNotNull null
      Pair(botName, it)
    }.toMap()

  override fun authenticate(authentication: Authentication): Authentication? {
    if (authentication !is TelegramBotToken) return null
    webhookRegistry.getBotByNameAndToken(authentication.botName, authentication.token)
      ?: throw InvalidBotCredentialsException(authentication.botName)
    return botAuthenticationProviders[authentication.botName]?.authenticate(authentication) ?: authentication
  }

  override fun supports(authentication: Class<*>): Boolean {
    return TelegramBotToken::class.java.isAssignableFrom(authentication)
  }

}