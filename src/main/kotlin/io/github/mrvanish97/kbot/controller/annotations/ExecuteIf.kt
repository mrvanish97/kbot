package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbot.controller.node.BotNodeExecutionPredicate
import io.github.mrvanish97.kbot.controller.node.BotNodeExecutionPredicateFactory
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@BotNodeAspect(
  annotationClass = ExecuteIf::class,
  processorBeanClass = ExecuteIfAspectProcessor::class
)
annotation class ExecuteIf(

  @get:AliasFor("aspectBeanName", annotation = BotNodeAspect::class)
  val predicateBeanName: String = "",

  @get:AliasFor("aspectBeanClass", annotation = BotNodeAspect::class)
  val predicateClass: KClass<out BotNodeExecutionPredicate> = BotNodeExecutionPredicate::class,

  @get:AliasFor("aspectFactoryBeanClass", annotation = BotNodeAspect::class)
  val predicateFactoryClass: KClass<out BotNodeExecutionPredicateFactory> = BotNodeExecutionPredicateFactory::class

)