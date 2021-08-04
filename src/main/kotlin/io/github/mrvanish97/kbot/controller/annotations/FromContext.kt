package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.controller.node.BotNodeContextConverter
import io.github.mrvanish97.kbot.controller.node.BotNodeContextConverterFactory
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@BotNodeAspect(
  processorBeanClass = BotNodeContextConverterAspectProcessor::class,
  annotationClass = FromContext::class
)
annotation class FromContext(

  @get:AliasFor("aspectBeanName", annotation = BotNodeAspect::class)
  val converterBeanName: String = "",

  @get:AliasFor("aspectBeanClass", annotation = BotNodeAspect::class)
  val converterBeanClass: KClass<out BotNodeContextConverter<*>> = BotNodeContextConverter::class,

  @get:AliasFor("aspectFactoryBeanClass", annotation = BotNodeAspect::class)
  val converterFactoryBeanClass: KClass<out BotNodeContextConverterFactory<*>> = BotNodeContextConverterFactory::class

)
