package io.github.mrvanish97.kbot

import io.github.mrvanish97.kbot.config.Bots
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeanNamesForAnnotation
import org.springframework.beans.factory.getBeansOfType
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext

val ApplicationContext.bots
  get() = getBean<Bots>()

inline fun <reified A : Annotation> ApplicationContext.getBotBeanNamesWithAnnotation(botName: String): Sequence<String> {
  return getBeanNamesForAnnotation<A>().asSequence()
    .filter(botBeanNamePredicate(botName))
}

inline fun <reified A : Annotation> ApplicationContext.getBotBeansWithAnnotation(botName: String): Sequence<Map.Entry<String, Any>> {
  return getBeansWithAnnotation<A>().asSequence()
    .filter(botBeanMapEntryPredicate(botName))
}

inline fun <reified A : Annotation, reified T : Any> ApplicationContext.getBotBeansOfType(
  botName: String,
  includeNonSingletons: Boolean = true,
  allowEagerInit: Boolean = true
): Sequence<Map.Entry<String, Any>> {
  return getBeansOfType<T>(includeNonSingletons, allowEagerInit).asSequence()
    .filter(botBeanMapEntryPredicate(botName))
}

fun ApplicationContext.getBotNameForBeanName(beanName: String) : String? {
  return findAnnotationOnBean(beanName, Bot::class.java)
      ?.value
      ?.takeIf { value -> value.isNotBlank() }
      ?: bots.names.takeIf { it.size == 1 }?.first()
}

@PublishedApi
internal fun ApplicationContext.botBeanMapEntryPredicate(botName: String) = fun(beanMapEnrty: Map.Entry<String, Any>): Boolean {
  return botBeanNamePredicate(botName)(beanMapEnrty.key)
}

@PublishedApi
internal fun ApplicationContext.botBeanNamePredicate(botName: String) = fun(beanName: String): Boolean {
  val bot = findAnnotationOnBean(beanName, Bot::class.java) ?: return false
  val botNameIsTheSame = bot.value == botName
  return if (bots.names.size == 1) {
    botNameIsTheSame || bot.value == ""
  } else {
    botNameIsTheSame
  }
}