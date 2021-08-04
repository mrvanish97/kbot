package io.github.mrvanish97.kbot.controller

import org.springframework.core.ResolvableType
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalGenericConverter
import org.springframework.core.convert.converter.ConverterRegistry
import org.springframework.core.convert.converter.GenericConverter
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.objects.Update

typealias BotSendMethods = Iterable<PartialBotApiMethod<*>>

@Suppress("FunctionName")
inline fun <reified T : Any> SendResultConverter(
  noinline function: SendResultConverterFunction<T>
): SendResultConverter<T> {
  return SendResultConverter(T::class.java, function)
}

typealias SendResultConverterFunction<T> = (update: Update, result: T) -> BotSendMethods?

class SendResultConverter<T : Any>(
  private val valueClass: Class<out T>,
  private val function: SendResultConverterFunction<T>
) : ConditionalGenericConverter {
  override fun getConvertibleTypes(): MutableSet<GenericConverter.ConvertiblePair> {
    return mutableSetOf(GenericConverter.ConvertiblePair(SendResult::class.java, Iterable::class.java))
  }

  override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
    if (source !is SendResult<*>) return null
    if (!valueClass.isAssignableFrom(source.result::class.java)) return null
    @Suppress("UNCHECKED_CAST")
    return function(source.update, source.result as T)
  }

  override fun matches(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean {
    return sourceType.type == SendResult::class.java
      && valueClass.isAssignableFrom(sourceType.resolvableType.getGeneric(0).toClass())
  }

}

data class SendResult<T : Any>(
  val update: Update,
  val result: T
)

inline fun <reified T : Any> ConverterRegistry.addSendResultConverter(
  noinline converter: (update: Update, result: T) -> BotSendMethods?
) {
  addConverter(SendResultConverter(converter))
}

inline fun <reified T : Any> ConverterRegistry.addSingeSendResultConverter(
  noinline converter: (update: Update, result: T) -> PartialBotApiMethod<*>?
) {
  addConverter(SendResultConverter { u: Update, r: T ->
    val result = converter(u, r)
    result?.let { sequenceOf(it).asIterable() }
  })
}

fun <T : Any> ConversionService.convertSendResult(update: Update, result: T): Iterable<PartialBotApiMethod<*>>? {
  val sendResult = SendResult(update, result)
  @Suppress("UNCHECKED_CAST")
  return convert(
    sendResult,
    TypeDescriptor(
      ResolvableType.forClassWithGenerics(
        SendResult::class.java,
        ResolvableType.forClass(result::class.java)
      ),
      null, null
    ),
    TypeDescriptor(
      ResolvableType.forClassWithGenerics(
        Iterable::class.java,
        ResolvableType.forClass(PartialBotApiMethod::class.java)
      ),
      null, null
    )
  ) as Iterable<PartialBotApiMethod<*>>?
}