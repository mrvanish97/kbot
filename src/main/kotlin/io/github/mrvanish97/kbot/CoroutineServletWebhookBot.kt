package io.github.mrvanish97.kbot

import org.telegram.telegrambots.meta.api.objects.Update
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.coroutines.CoroutineContext

interface CoroutineServletWebhookBot {

  suspend fun onWebhookUpdateReceived(
    update: Update,
    request: HttpServletRequest,
    response: HttpServletResponse,
    coroutineContextFactory: () -> CoroutineContext
  )

}