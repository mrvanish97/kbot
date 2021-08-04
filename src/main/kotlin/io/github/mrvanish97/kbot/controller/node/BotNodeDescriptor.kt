package io.github.mrvanish97.kbot.controller.node

import java.lang.reflect.Method

interface BotNodeDescriptor {
  val bean: Any
  val beanType: Class<*>
  val method: Method
  val properties: BotNodeProperties
}

internal data class BotNodeDescriptorImpl(
  override val bean: Any,
  override val beanType: Class<*>,
  override val method: Method,
  override val properties: BotNodeProperties
): BotNodeDescriptor