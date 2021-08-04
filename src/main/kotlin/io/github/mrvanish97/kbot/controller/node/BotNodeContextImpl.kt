package io.github.mrvanish97.kbot.controller.node

import org.springframework.security.core.Authentication
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal data class BotNodeContextImpl(
  override val sender: AbsSender,
  override val update: Update,
  override val request: HttpServletRequest,
  override val response: HttpServletResponse,
  override val auth: Authentication
) : BotNodeContext