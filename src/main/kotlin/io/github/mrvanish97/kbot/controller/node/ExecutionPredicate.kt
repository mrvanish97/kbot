package io.github.mrvanish97.kbot.controller.node

fun interface ExecutionPredicate<T : Any> {

  fun test(context: BotNodeContext, value: T) : Boolean

}