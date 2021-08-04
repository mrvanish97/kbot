package io.github.mrvanish97.kbot.controller.node

fun interface BotNodeExecutionPredicate {

  fun isExecutable(context: BotNodeContext) : Boolean

}


fun List<BotNodeExecutionPredicate>.buildConjunction(): BotNodeExecutionPredicate {
  return when (size) {
    0 -> BotNodeExecutionPredicate { false }
    1 -> first()
    else -> BotNodeExecutionPredicate {
      all { predicate -> predicate.isExecutable(it) }
    }
  }
}