package io.github.mrvanish97.kbot.controller

import io.github.mrvanish97.kbot.CoroutineServletWebhookBot
import io.github.mrvanish97.kbot.config.WebhookRegistry
import io.github.mrvanish97.kbot.security.InvalidBotCredentialsException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.telegram.telegrambots.Constants
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.WebhookBot
import java.util.concurrent.Executor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.coroutines.CoroutineContext

@RestController
@RequestMapping(Constants.WEBHOOK_URL_PATH)
class RootBotController @Autowired constructor(
  private val registry: WebhookRegistry,
  @Qualifier(EXECUTOR_FACTORY_BEAN_NAME)
  private val executorFactory: (SecurityContext) -> Executor
) {

  companion object {
    const val EXECUTOR_FACTORY_BEAN_NAME =
      "io.github.mrvanish97.kbot.api.controller.RootBotController.executorFactoryBean"
  }

  private fun buildCoroutineContextFactory() = fun(): CoroutineContext {
    return executorFactory(SecurityContextHolder.getContext()).asCoroutineDispatcher()
  }

  @PostMapping("/{botName}")
  suspend fun onUpdate(
    @PathVariable botName: String,
    @RequestBody update: Update,
    request: HttpServletRequest,
    response: HttpServletResponse
  ): BotApiMethod<*>? {
    val bot = getBot(botName)
    return if (bot is CoroutineServletWebhookBot) {
      CoroutineScope(buildCoroutineContextFactory()()).launch {
        bot.onWebhookUpdateReceived(update, request, response, buildCoroutineContextFactory())
      }
      return null
    } else {
      bot.onWebhookUpdateReceived(update)
    }
  }

  private fun getBot(botName: String): WebhookBot {
    return registry.getBotByName(botName) ?: throw InvalidBotCredentialsException(botName)
  }

}

