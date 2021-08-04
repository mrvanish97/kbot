package io.github.mrvanish97.kbot.controller.node

import java.lang.reflect.Method
import java.lang.reflect.Parameter

internal data class BotNodeParameterDescriptorImpl(
  override val method: Method,
  override val parameter: Parameter,
  override val botNodeName: String,
  override val controllerBean: Any,
  override val controllerType: Class<*>
): BotNodeParameterDescriptor