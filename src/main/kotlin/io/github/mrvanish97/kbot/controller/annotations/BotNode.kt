package io.github.mrvanish97.kbot.controller.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class BotNode(
  val value: String = ""
)
