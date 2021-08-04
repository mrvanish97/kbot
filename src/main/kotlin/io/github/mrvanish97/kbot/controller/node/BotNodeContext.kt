package io.github.mrvanish97.kbot.controller.node

import org.springframework.security.core.Authentication
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.bots.AbsSender
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface BotNodeContext {
  val sender: AbsSender
  val update: Update
  val request: HttpServletRequest
  val response: HttpServletResponse
  val auth: Authentication
}