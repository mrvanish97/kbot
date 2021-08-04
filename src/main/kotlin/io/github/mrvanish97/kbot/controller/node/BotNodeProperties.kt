package io.github.mrvanish97.kbot.controller.node

interface BotNodeProperties {
  val botNodeName: String
  val parametersInfo: List<BotNodeParameterInfo<*>>
  fun isExecutable(context: BotNodeContext): Boolean
}

data class BotNodePropertiesImpl(
  override val botNodeName: String,
  override val parametersInfo: List<BotNodeParameterInfo<*>>,
  private val isExecutableCallback: BotNodeExecutionPredicate
) : BotNodeProperties {
  override fun isExecutable(context: BotNodeContext): Boolean {
    return isExecutableCallback.isExecutable(context)
  }
}