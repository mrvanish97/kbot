package io.github.mrvanish97.kbot.security

import org.springframework.security.core.AuthenticatedPrincipal
import org.telegram.telegrambots.meta.api.objects.User

fun interface TelegramUserPrinciple : AuthenticatedPrincipal, TelegramUserProvider {
  override fun getName(): String {
    return getUser().id.toString()
  }
}

fun interface TelegramUserProvider {
  fun getUser(): User
}