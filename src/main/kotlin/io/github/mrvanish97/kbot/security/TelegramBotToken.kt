package io.github.mrvanish97.kbot.security

import io.github.mrvanish97.kbot.utils.Cleanable
import io.github.mrvanish97.kbot.utils.CleanableString
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.telegram.telegrambots.meta.api.objects.User

class TelegramBotToken @JvmOverloads constructor(
  private val user: User,
  val botName: String,
  authorities: Collection<GrantedAuthority>? = null,
  val token: CharSequence = CleanableString.EMPTY
) : AbstractAuthenticationToken(authorities) {

  companion object {
    @JvmField
    val NOT_A_USER = User(-1, "@not_a_user", true)
  }

  override fun getCredentials() = null

  override fun getPrincipal(): TelegramUserPrinciple = TelegramUserPrinciple {
    return@TelegramUserPrinciple with(this@TelegramBotToken.user) {
      User(
        id,
        firstName,
        isBot,
        lastName,
        userName,
        languageCode,
        canJoinGroups,
        canReadAllGroupMessages,
        supportInlineQueries
      )
    }
  }

  override fun eraseCredentials() {
    (token as? Cleanable)?.cleanIfNeeded()
  }

}