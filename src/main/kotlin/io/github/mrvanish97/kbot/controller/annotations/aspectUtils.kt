package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbnsext.javaName
import io.github.mrvanish97.kbot.controller.node.BotNodeAspectAbstractFactory
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.util.ClassUtils
import java.lang.reflect.AnnotatedElement

sealed interface ContextLookupResult

private class AspectProcessorNotDefinedException(
  userAnnotationAttributes: AnnotationAttributes,
  context: ApplicationContext
) : Exception(
  "Could not find any ${BotNodeAspectProcessor::class.javaName} in given context $context for ${userAnnotationAttributes.annotationType()}"
)

class BeanLookupResult(val bean: Any) : ContextLookupResult

class FactoryLookupResult(val factory: BotNodeAspectAbstractFactory<*, *>) : ContextLookupResult

object EmptyLookupResult : ContextLookupResult

inline fun <reified A : Annotation> lookupBotNodeAspect(
  element: AnnotatedElement,
  context: ApplicationContext
): ContextLookupResult {
  return lookupBotNodeAspect(element, context, A::class.java)
}

private fun makeLookupResult(bean: Any, aspectProcessor: BotNodeAspectProcessor<*>): ContextLookupResult {
  val beanClass = ClassUtils.getUserClass(bean)
  return when {
    aspectProcessor.beanResolvableType.isAssignableFrom(beanClass) -> {
      BeanLookupResult(bean)
    }
    else -> {
      try {
        FactoryLookupResult(aspectProcessor.processRawFactoryBean(bean))
      } catch (e: InvalidAbstractFactoryBeanException) {
        throw IllegalArgumentException("Bean $bean is not a compatible factory bean", e)
      }
    }
  }
}

fun lookupBotNodeAspect(
  element: AnnotatedElement,
  context: ApplicationContext,
  annotationClass: Class<out Annotation>
): ContextLookupResult {
  val botNodeAspectFromUsersAnnotation = AnnotatedElementUtils.findMergedAnnotationAttributes(
    element, BotNodeAspect::class.java, true, true
  ) ?: throw IllegalArgumentException("$annotationClass is not annotated with ${BotNodeAspect::class.java}")
  val aspectProcessor = try {
    getAspectProcessor(botNodeAspectFromUsersAnnotation, context)
  } catch (e: AspectProcessorNotDefinedException) {
    throw IllegalArgumentException(
      "$annotationClass should be annotated with ${BotNodeAspect::class.javaName}, which has defined either " +
        "${BotNodeAspect::aspectBeanName.name}, or ${BotNodeAspect::aspectBeanClass.name}",
      e
    )
  }
  val annotation = AnnotationUtils.synthesizeAnnotation(annotationClass)
  val mergedAnnotations = MergedAnnotations.from(annotation)
  val mergedBotNodeAspect = mergedAnnotations[BotNodeAspect::class.java].synthesize()
  val defaultBeanName = mergedBotNodeAspect.aspectBeanName
  val aspectBeanName = botNodeAspectFromUsersAnnotation.getString(BotNodeAspect::aspectBeanName.name)
  if (aspectBeanName != defaultBeanName) {
    val bean = context.getBean(aspectBeanName)
    return makeLookupResult(bean, aspectProcessor)
  }
  val beanClassName = botNodeAspectFromUsersAnnotation.getString(BotNodeAspect::aspectBeanClass.name)
  val defaultBeanClassName = mergedBotNodeAspect.aspectBeanClass.javaName
  if (beanClassName != defaultBeanClassName) {
    val beanClass = ClassUtils.resolveClassName(beanClassName, context.classLoader)
    val bean = context.getBean(beanClass)
    return makeLookupResult(bean, aspectProcessor)
  }
  val beanFactoryClassName = botNodeAspectFromUsersAnnotation.getString(BotNodeAspect::aspectFactoryBeanClass.name)
  val defaultFactoryClassName = mergedBotNodeAspect.aspectFactoryBeanClass
  if (beanFactoryClassName != defaultFactoryClassName.javaName) {
    @Suppress("UNCHECKED_CAST")
    val beanClass = ClassUtils.resolveClassName(beanFactoryClassName, context.classLoader) as Class<out BotNodeAspectAbstractFactory<*, *>>
    val bean = context.getBean(beanClass)
    if (aspectProcessor.beanFactoryResolvableType.isAssignableFrom(beanClass)) {
      return FactoryLookupResult(bean)
    } else {
      throw IllegalArgumentException(
        "${aspectProcessor.beanFactoryResolvableType} is not assignable from ${beanClass.name}"
      )
    }
  }
  return EmptyLookupResult
}

@Throws(AspectProcessorNotDefinedException::class)
private fun getAspectProcessor(
  userAnnotationAttributes: AnnotationAttributes,
  context: ApplicationContext
): BotNodeAspectProcessor<*> {
  val beanName = userAnnotationAttributes.getString(BotNodeAspect::processorBeanName.name)
  if (beanName.isNotBlank()) {
    return context.getBean(beanName, BotNodeAspectProcessor::class.java)
  }
  val beanClassName = userAnnotationAttributes.getString(BotNodeAspect::processorBeanClass.name)
  if (beanClassName != BotNodeAspectProcessor::class.javaName) {
    val beanClass = ClassUtils.resolveClassName(beanClassName, context.classLoader)
    if (BotNodeAspectProcessor::class.java.isAssignableFrom(beanClass)) {
      return context.getBean(beanClass) as BotNodeAspectProcessor<*>
    }
  }
  throw AspectProcessorNotDefinedException(userAnnotationAttributes, context)
}