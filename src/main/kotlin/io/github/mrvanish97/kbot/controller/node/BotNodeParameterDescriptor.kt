package io.github.mrvanish97.kbot.controller.node

import java.lang.reflect.Parameter

interface BotNodeParameterDescriptor : BotNodeMethodDescriptor{
  val parameter: Parameter
}