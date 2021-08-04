package io.github.mrvanish97.kbot.controller.annotations

@Suppress("DEPRECATED_JAVA_ANNOTATION")
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
@java.lang.annotation.Repeatable(VariableResolvers::class)
annotation class VariableResolver(
  val name: String,
  val expression: String
)
