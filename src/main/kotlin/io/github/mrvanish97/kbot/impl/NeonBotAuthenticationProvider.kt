package io.github.mrvanish97.kbot.impl

import io.github.mrvanish97.kbot.Bot
import io.github.mrvanish97.kbot.security.BotAuthenticationProvider
import io.github.mrvanish97.kbot.security.TelegramBotToken
import org.springframework.core.io.ClassPathResource
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.stereotype.Component
import java.util.*

@Bot("neon-fonts")
@Component
class NeonBotAuthenticationProvider : BotAuthenticationProvider<TelegramBotToken> {

  private val neonUserDetailService = InMemoryUserDetailsManager(
    Properties().apply {
      load(ClassPathResource("neon-bot/users.properties", javaClass.classLoader).inputStream)
    }
  )

  override fun authenticate(auth: TelegramBotToken): TelegramBotToken {
    val user = neonUserDetailService.loadUserByUsername(auth.name)
    return TelegramBotToken(auth.principal.getUser(), auth.botName, user.authorities)
  }
}