package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.controller.node.ExpressionConverterFactory

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@FromContext(converterFactoryBeanClass = ExpressionConverterFactory::class)
annotation class ExpressionConverter(
  vararg val expressions: String
)
