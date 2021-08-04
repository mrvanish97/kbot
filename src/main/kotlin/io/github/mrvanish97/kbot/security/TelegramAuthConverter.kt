package io.github.mrvanish97.kbot.security

import io.github.mrvanish97.kbot.config.BotPathEncoder
import io.github.mrvanish97.kbot.user
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.web.client.RestTemplate
import org.telegram.telegrambots.Constants.WEBHOOK_URL_PATH
import org.telegram.telegrambots.meta.api.objects.Update
import javax.servlet.http.HttpServletRequest

@Suppress("UNCHECKED_CAST")
private val messageConverter = RestTemplate().messageConverters.find {
  it.canRead(Update::class.java, MediaType.APPLICATION_JSON)
} as? HttpMessageConverter<Update>

private const val DELIMITER = "$WEBHOOK_URL_PATH/"

class TelegramAuthConverter(
  private val encoder: BotPathEncoder
) : AuthenticationConverter {

  override fun convert(request: HttpServletRequest): Authentication {
    val httpRequest = ServletServerHttpRequest(request)
    val user = runCatching {
      messageConverter?.read(Update::class.java, httpRequest)
    }.getOrNull()?.user ?: TelegramBotToken.NOT_A_USER

    val (name, token) = encoder.decode(httpRequest.uri.toString().substringAfter(DELIMITER))

    return TelegramBotToken(user, name, token = token)
  }
}