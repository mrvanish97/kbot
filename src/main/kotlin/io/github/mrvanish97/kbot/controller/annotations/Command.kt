package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.controller.node.CommandExecutionPredicateFactory

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
@AnyTextMessageVariableResolver
@ExecuteIf(predicateFactoryClass = CommandExecutionPredicateFactory::class)
annotation class Command(
  val name: String
)