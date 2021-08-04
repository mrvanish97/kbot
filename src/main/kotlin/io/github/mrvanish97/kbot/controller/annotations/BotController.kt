package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.Bot
import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Controller

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@MustBeDocumented
@Controller
@Bot
annotation class BotController(

  @get:AliasFor("value", annotation = Bot::class)
  val botName: String = "",

  @get:AliasFor(annotation = Controller::class)
  val value: String = ""

)
