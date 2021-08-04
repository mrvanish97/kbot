package io.github.mrvanish97.kbot.controller.annotations

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@VariableResolver("anyMessage", "T(io.github.mrvanish97.kbot.TelegramBotApiExt).getAnyMessage(update)")
annotation class AnyMessageVariableResolver
