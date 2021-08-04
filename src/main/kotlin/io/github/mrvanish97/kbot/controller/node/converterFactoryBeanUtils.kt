//package io.github.mrvanish97.kbot.controller.node
//
//import io.github.mrvanish97.kbnsext.javaName
//import org.springframework.core.ResolvableType
//import java.lang.reflect.Method
//import java.util.function.Function
//import java.util.function.Supplier
//import kotlin.reflect.jvm.javaMethod
//
//private data class DesiredType(
//  val type: Class<*>,
//  val generics: List<DesiredGenericType> = emptyList(),
//  val isKotlinType: Boolean = false
//) {
//
//  private fun stringForDesiredGenericTypeWithType(it: BoundedGenericType): String {
//    return if (isKotlinType) {
//      when (it.bounds) {
//        Bounds.EXACT -> it.type.name
//        Bounds.SUPER -> "in ${it.type.name}"
//        Bounds.EXTENDS -> "out ${it.type.name}"
//      }
//    } else {
//      when (it.bounds) {
//        Bounds.EXACT -> it.type.name
//        Bounds.SUPER -> "? super ${it.type.name}"
//        Bounds.EXTENDS -> "? extends ${it.type.name}"
//      }
//    }
//  }
//
//  override fun toString(): String {
//    val prefix = type.name
//    val postfix = if (generics.isNotEmpty()) {
//      generics.joinToString(prefix = "<", separator = ", ", postfix = ">") {
//        when (it) {
//          is BoundedGenericType -> stringForDesiredGenericTypeWithType(it)
//          is Wildcard -> if (isKotlinType) "*" else "?"
//        }
//      }
//    } else {
//      ""
//    }
//    return prefix + postfix
//  }
//}
//
//private sealed interface DesiredGenericType
//
//private object Wildcard : DesiredGenericType
//
//private class BoundedGenericType(val type: Class<*>, val bounds: Bounds) : DesiredGenericType
//
//private enum class Bounds {
//  EXACT, SUPER, EXTENDS
//}
//
//
//fun buildInvalidTypeMessage(bean: Any) =
//  "Provided converter factory bean $bean should be either instance of ${BotNodeContextConverter::class.javaName}, " +
//    "or ${BotNodeContextConverterFactory::class.javaName}, " +
//    "or ${Supplier::class.javaName}, or ${Function::class.javaName}, " +
//    "or ${Function0::class.javaName}, or ${Function1::class.javaName}"
//
//internal class InvalidConverterFactoryBeanType(message: String = "") : Exception(message)
//
//@Suppress("UNCHECKED_CAST")
//@Throws(InvalidConverterFactoryBeanType::class)
//internal fun processRawConverterFactoryBean(bean: Any): BotNodeContextConverterFactory<*> {
//  return when (bean) {
//    is BotNodeContextConverter<*> -> BotNodeContextConverterFactory { bean }
//    is BotNodeContextConverterFactory<*> -> bean as BotNodeContextConverterFactory<Any>
//    is Supplier<*> -> processJavaSupplier(bean)
//    is Function<*, *> -> processJavaFunction(bean)
//    is Function0<*> -> processKotlinFunction0(bean)
//    is Function1<*, *> -> processKotlinFunction1(bean)
//    else -> throw InvalidConverterFactoryBeanType(buildInvalidTypeMessage(bean))
//  }
//}
//
//private val converterType = ResolvableType.forType(BotNodeContextConverter::class.java)
//
//private fun checkMethod(method: Method): Boolean {
//  val conjunctionMember = when (method.parameterCount) {
//    0 -> true
//    1 -> ResolvableType.forMethodParameter(method, 0).isAssignableFrom(BotNodeExtraContextData::class.java)
//    else -> throw InvalidConverterFactoryBeanType("Method $method should have either no parameters, or only one")
//  }
//  return conjunctionMember && converterType.isAssignableFrom(ResolvableType.forMethodReturnType(method))
//}
//
//private val noArgGenerics = listOf(
//  BoundedGenericType(
//    type = BotNodeContextConverter::class.java,
//    bounds = Bounds.EXTENDS
//  )
//)
//
//private val oneArgGenerics = listOf(
//  BoundedGenericType(
//    type = BotNodeExtraContextData::class.java,
//    bounds = Bounds.SUPER
//  ),
//  BoundedGenericType(
//    type = BotNodeContextConverter::class.java,
//    bounds = Bounds.EXTENDS
//  )
//)
//
//private fun processJavaSupplier(supplier: Supplier<*>): BotNodeContextConverterFactory<*> {
//  val method = supplier::get.javaMethod
//    ?: throw InvalidConverterFactoryBeanType("For some reason, supplier $supplier has no 'get' method")
//  val desiredType = DesiredType(
//    type = Supplier::class.java,
//    generics = noArgGenerics
//  )
//  return processMethod(supplier, method, desiredType) {
//    @Suppress("UNCHECKED_CAST")
//    (supplier as Supplier<out BotNodeContextConverter<*>>).get()
//  }
//}
//
//private fun processJavaFunction(function: Function<*, *>): BotNodeContextConverterFactory<*> {
//  val method = function::apply.javaMethod
//    ?: throw InvalidConverterFactoryBeanType("For some reason, function $function has no 'apply' method")
//  val desiredType = DesiredType(
//    type = Function::class.java,
//    generics = oneArgGenerics
//  )
//  return processMethod(function, method, desiredType) {
//    @Suppress("UNCHECKED_CAST")
//    (function as Function<in BotNodeExtraContextData, out BotNodeContextConverter<*>>).apply(it)
//  }
//}
//
//private fun processMethod(
//  bean: Any,
//  method: Method,
//  desiredType: DesiredType,
//  buildConverter: (BotNodeExtraContextData) -> BotNodeContextConverter<*>
//): BotNodeContextConverterFactory<*> {
//  return if (checkMethod(method)) {
//    BotNodeContextConverterFactory {
//      buildConverter(it)
//    }
//  } else {
//    throw InvalidConverterFactoryBeanType(buildErrorMessageForKnownType(bean, desiredType))
//  }
//}
//
//private fun processKotlinFunction0(function: Function0<*>): BotNodeContextConverterFactory<*> {
//  val method = function::invoke.javaMethod
//    ?: throw InvalidConverterFactoryBeanType("For some reason, function $function has no 'apply' method")
//  val desiredType = DesiredType(
//    type = Function0::class.java,
//    generics = noArgGenerics,
//    isKotlinType = true
//  )
//  return processMethod(function, method, desiredType) {
//    @Suppress("UNCHECKED_CAST")
//    (function as Function0<BotNodeContextConverter<*>>).invoke()
//  }
//}
//
//private fun processKotlinFunction1(function: Function1<*, *>): BotNodeContextConverterFactory<*> {
//  val method = function::invoke.javaMethod
//    ?: throw InvalidConverterFactoryBeanType("For some reason, function $function has no 'apply' method")
//  val desiredType = DesiredType(
//    type = Function1::class.java,
//    generics = oneArgGenerics,
//    isKotlinType = true
//  )
//  return processMethod(function, method, desiredType) {
//    @Suppress("UNCHECKED_CAST")
//    (function as Function1<BotNodeExtraContextData, BotNodeContextConverter<*>>).invoke(it)
//  }
//}
//
//private fun buildErrorMessageForKnownType(bean: Any, desiredType: DesiredType) = "$bean should be of type $desiredType"