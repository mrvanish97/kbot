package io.github.mrvanish97.kbot.security

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class InvalidBotCredentialsException(botName: String) : AuthenticationException("Bot '$botName' is not registered")