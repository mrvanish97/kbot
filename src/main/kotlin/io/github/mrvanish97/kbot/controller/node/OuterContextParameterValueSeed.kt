package io.github.mrvanish97.kbot.controller.node

internal class OuterContextParameterValueSeed<T : Any>(
  val requestedClass: Class<out T>,
  override val isRequired: Boolean
) : ParameterValueSeed, BotNodeParameterInfo<T>