package io.github.mrvanish97.kbot.impl

import io.github.mrvanish97.kbot.controller.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.*

@BotController("neon-fonts")
class NeonBotController @Autowired constructor(
  private val fontRenderer: FontRendererService,
) {

//  @BotNode
//  suspend fun makeUppercase(
//    @FromAnyUpdateMessageText message: String,
//    update: Update,
//    sender: AbsSender,
//    request: HttpServletRequest,
//    scope: CoroutineScope
//  ): String {
//    return message.uppercase(Locale.getDefault())
//  }

  @BotNode
  @Command("reversed")
  suspend fun makeReversed(
    @CommandQuery command: String,
    @AnyTextMessage message: String,
  ): String {
    return command.reversed()
  }


  @BotNode
  @ExpressionPredicate("")
  suspend fun lol(
    @AnyTextMessage message: String,
  ): String {
    return message.uppercase(Locale.getDefault())
  }

}