package io.github.mrvanish97.kbot.controller.annotations

import org.springframework.core.convert.converter.Converter
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class WithExecutionPredicate(
  val value: String = "",
  val executionPredicateValueName: String = "",
)
