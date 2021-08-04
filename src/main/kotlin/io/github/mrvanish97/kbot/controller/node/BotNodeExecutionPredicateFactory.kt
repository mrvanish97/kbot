package io.github.mrvanish97.kbot.controller.node

import org.springframework.core.ResolvableType

private val type = ResolvableType.forClass(BotNodeExecutionPredicate::class.java)

fun interface BotNodeExecutionPredicateFactory :
  BotNodeAspectAbstractFactory<BotNodeMethodDescriptor, BotNodeExecutionPredicate> {

  override fun getResolvableType() = type

}

