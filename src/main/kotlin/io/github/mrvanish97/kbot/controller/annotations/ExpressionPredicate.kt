package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.controller.node.ExpressionExecutionPredicateFactory

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ExecuteIf(predicateFactoryClass = ExpressionExecutionPredicateFactory::class)
annotation class ExpressionPredicate(
  vararg val expressions: String
)
