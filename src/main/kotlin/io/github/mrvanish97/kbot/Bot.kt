package io.github.mrvanish97.kbot

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class Bot(
  val value: String = ""
)
