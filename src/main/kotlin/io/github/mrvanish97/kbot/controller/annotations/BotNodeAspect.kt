package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.controller.node.BotNodeAspectAbstractFactory
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class BotNodeAspect(

  val aspectBeanName: String = "",

  val aspectBeanClass: KClass<*> = Any::class,

  val aspectFactoryBeanClass: KClass<out BotNodeAspectAbstractFactory<*, *>> = BotNodeAspectAbstractFactory::class,

  val processorBeanName: String = "",

  val processorBeanClass: KClass<out BotNodeAspectProcessor<*>> = BotNodeAspectProcessor::class,

  val annotationClass: KClass<out Annotation>

)
