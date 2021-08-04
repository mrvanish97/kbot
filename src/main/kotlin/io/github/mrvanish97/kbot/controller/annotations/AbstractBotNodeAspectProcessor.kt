package io.github.mrvanish97.kbot.controller.annotations

import io.github.mrvanish97.kbnsext.javaName
import io.github.mrvanish97.kbot.controller.node.BotNodeAspectAbstractFactory
import io.github.mrvanish97.kbot.controller.node.BotNodeParameterDescriptor
import org.springframework.core.ResolvableType
import org.springframework.util.ClassUtils
import java.lang.reflect.Method
import java.util.function.Function
import java.util.function.Supplier
import kotlin.reflect.jvm.javaMethod

abstract class AbstractBotNodeAspectProcessor<C, F : BotNodeAspectAbstractFactory<C, *>>(
  private val converterResultType: ResolvableType
) : BotNodeAspectProcessor<F> {

  private val noArgGenerics by lazy { listOf(BoundedGenericType(converterResultType, Bounds.EXTENDS)) }

  private val oneArgGenerics by lazy {
    listOf(
      BoundedGenericType(ResolvableType.forClass(BotNodeParameterDescriptor::class.java), Bounds.SUPER),
      BoundedGenericType(converterResultType, Bounds.EXTENDS)
    )
  }

  private fun buildInvalidTypeMessage(bean: Any) =
    "Provided converter factory bean $bean should be either instance of ${beanFactoryResolvableType.toClass()}, " +
      "or ${Supplier::class.javaName}, or ${Function::class.javaName}, " +
      "or ${Function0::class.javaName}, or ${Function1::class.javaName}"

  @Suppress("UNCHECKED_CAST")
  @Throws(InvalidAbstractFactoryBeanException::class)
  override fun processRawFactoryBean(factoryBean: Any): F {
    val beanClass = ClassUtils.getUserClass(factoryBean)
    return when (factoryBean) {
      beanFactoryResolvableType.isAssignableFrom(beanClass) -> factoryBean as F
      is Supplier<*> -> processJavaSupplier(factoryBean)
      is Function<*, *> -> processJavaFunction(factoryBean)
      is Function0<*> -> processKotlinFunction0(factoryBean)
      is Function1<*, *> -> processKotlinFunction1(factoryBean)
      else -> throw InvalidAbstractFactoryBeanException(buildInvalidTypeMessage(factoryBean))
    }
  }

  private fun checkMethod(method: Method): Boolean {
    val conjunctionMember = when (method.parameterCount) {
      0 -> true
      1 -> ResolvableType.forMethodParameter(method, 0).isAssignableFrom(converterResultType)
      else -> throw InvalidAbstractFactoryBeanException("Method $method should have either no parameters, or only one")
    }
    return conjunctionMember && converterResultType.isAssignableFrom(ResolvableType.forMethodReturnType(method))
  }

  protected abstract fun buildFactory(converter: (C) -> Any): F

  private fun processMethod(
    bean: Any,
    method: Method,
    desiredType: DesiredType,
    buildConverter: (C) -> Any
  ): F {
    return if (checkMethod(method)) {
      buildFactory(buildConverter)
    } else {
      throw InvalidAbstractFactoryBeanException(buildErrorMessageForKnownType(bean, desiredType))
    }
  }

  private fun processJavaSupplier(supplier: Supplier<*>): F {
    val method = supplier::get.javaMethod
      ?: throw InvalidAbstractFactoryBeanException("For some reason, supplier $supplier has no 'get' method")
    val desiredType = DesiredType(
      type = Supplier::class.java,
      generics = noArgGenerics
    )
    return processMethod(supplier, method, desiredType) {
      supplier.get()
    }
  }

  private fun processJavaFunction(function: Function<*, *>): F {
    val method = function::apply.javaMethod
      ?: throw InvalidAbstractFactoryBeanException("For some reason, function $function has no 'apply' method")
    val desiredType = DesiredType(
      type = Function::class.java,
      generics = oneArgGenerics
    )
    return processMethod(function, method, desiredType) {
      @Suppress("UNCHECKED_CAST")
      (function as Function<in C, *>).apply(it)
    }
  }

  private fun processKotlinFunction0(function: Function0<*>): F {
    val method = function::invoke.javaMethod
      ?: throw InvalidAbstractFactoryBeanException("For some reason, function $function has no 'apply' method")
    val desiredType = DesiredType(
      type = Function0::class.java,
      generics = noArgGenerics,
      isKotlinType = true
    )
    return processMethod(function, method, desiredType) {
      @Suppress("UNCHECKED_CAST")
      (function as Function0<Any>).invoke()
    }
  }


  private fun processKotlinFunction1(function: Function1<*, *>): F {
    val method = function::invoke.javaMethod
      ?: throw InvalidAbstractFactoryBeanException("For some reason, function $function has no 'apply' method")
    val desiredType = DesiredType(
      type = Function1::class.java,
      generics = oneArgGenerics,
      isKotlinType = true
    )
    return processMethod(function, method, desiredType) {
      @Suppress("UNCHECKED_CAST")
      (function as Function1<C, Any>).invoke(it)
    }
  }

}

private data class DesiredType(
  val type: Class<*>,
  val generics: List<DesiredGenericType> = emptyList(),
  val isKotlinType: Boolean = false
) {
  private fun stringForDesiredGenericTypeWithType(it: BoundedGenericType): String {
    return if (isKotlinType) {
      when (it.bounds) {
        Bounds.EXACT -> it.type.toString()
        Bounds.SUPER -> "in ${it.type}"
        Bounds.EXTENDS -> "out ${it.type}"
      }
    } else {
      when (it.bounds) {
        Bounds.EXACT -> it.type.toString()
        Bounds.SUPER -> "? super ${it.type}"
        Bounds.EXTENDS -> "? extends ${it.type}"
      }
    }
  }

  override fun toString(): String {
    val prefix = type.name
    val postfix = if (generics.isNotEmpty()) {
      generics.joinToString(prefix = "<", separator = ", ", postfix = ">") {
        when (it) {
          is BoundedGenericType -> stringForDesiredGenericTypeWithType(it)
          is Wildcard -> if (isKotlinType) "*" else "?"
        }
      }
    } else {
      ""
    }
    return prefix + postfix
  }

}

private sealed interface DesiredGenericType
private object Wildcard : DesiredGenericType
private class BoundedGenericType(val type: ResolvableType, val bounds: Bounds) : DesiredGenericType
private enum class Bounds {
  EXACT, SUPER, EXTENDS
}

private fun buildErrorMessageForKnownType(bean: Any, desiredType: DesiredType) = "$bean should be of type $desiredType"