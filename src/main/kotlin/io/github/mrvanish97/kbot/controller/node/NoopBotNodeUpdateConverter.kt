package io.github.mrvanish97.kbot.controller.node

object NoopBotNodeUpdateConverter : BotNodeContextConverter<BotNodeContext> {
  override fun convertContext(context: BotNodeContext): BotNodeContext {
    return context
  }
}