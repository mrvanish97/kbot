package io.github.mrvanish97.kbot.controller.node

import java.lang.reflect.Method

internal data class BotNodeMethodDescriptorImpl(
  override val controllerBean: Any,
  override val controllerType: Class<*>,
  override val method: Method,
  override val botNodeName: String
) : BotNodeMethodDescriptor
