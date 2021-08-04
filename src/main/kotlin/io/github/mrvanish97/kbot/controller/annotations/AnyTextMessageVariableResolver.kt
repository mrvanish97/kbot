package io.github.mrvanish97.kbot.controller.annotations

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@AnyMessageVariableResolver
@VariableResolver("anyTextMessage", "#anyMessage.text")
annotation class AnyTextMessageVariableResolver