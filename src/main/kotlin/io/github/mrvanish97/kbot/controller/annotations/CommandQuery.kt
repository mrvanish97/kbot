package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.controller.node.CommandQueryContextConverterFactory

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
@FromContext(converterFactoryBeanClass = CommandQueryContextConverterFactory::class)
annotation class CommandQuery
