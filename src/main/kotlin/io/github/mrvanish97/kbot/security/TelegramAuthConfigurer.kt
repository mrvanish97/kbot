package io.github.mrvanish97.kbot.security

import io.github.mrvanish97.kbot.config.BotPathEncoder
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.HttpSecurityBuilder
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.context.SecurityContextPersistenceFilter

class TelegramAuthConfigurer<H : HttpSecurityBuilder<H>> constructor(
  private val botPathEncoder: BotPathEncoder
) : AbstractHttpConfigurer<TelegramAuthConfigurer<H>, H>() {

  override fun init(builder: H) {
    // There's no need to init this
  }

  override fun configure(builder: H) {
    val authenticationManager = builder.getSharedObject(AuthenticationManager::class.java)
    val authFilter = TelegramAuthFilter(authenticationManager, botPathEncoder)
    authFilter.afterPropertiesSet()
    builder.addFilterAfter(authFilter, SecurityContextPersistenceFilter::class.java)
  }

}