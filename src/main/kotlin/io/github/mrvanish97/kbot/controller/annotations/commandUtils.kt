package io.github.mrvanish97.kbot.controller.annotations

import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.AnnotatedElement

fun getCommandName(element: AnnotatedElement): String {
  return AnnotatedElementUtils
    .findMergedAnnotationAttributes(element, Command::class.java, true, true)
    ?.getString(Command::name.name) ?: ""
}

fun commandNameToMessage(commandName: String) = "/$commandName"