package io.github.mrvanish97.kbot.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

fun interface BotAuthenticationProvider<A : Authentication> {

  companion object {
    val noop = BotAuthenticationProvider {
      return@BotAuthenticationProvider it
    }
  }

  @Throws(AuthenticationException::class)
  fun authenticate(auth: TelegramBotToken): A

}