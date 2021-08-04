package io.github.mrvanish97.kbot.controller.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@MustBeDocumented
annotation class NodePredicate(
  vararg val expressions: String
)
