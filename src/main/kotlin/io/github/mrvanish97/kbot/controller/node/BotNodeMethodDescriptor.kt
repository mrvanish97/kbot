package io.github.mrvanish97.kbot.controller.node

import java.lang.reflect.Method

interface BotNodeMethodDescriptor {

  val controllerBean: Any

  val controllerType: Class<*>

  val method: Method

  val botNodeName: String

}