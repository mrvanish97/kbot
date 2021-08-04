package io.github.mrvanish97.kbot.controller.node

import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.ConditionalGenericConverter
import org.springframework.core.convert.converter.GenericConverter

fun interface BotNodeContextConverter<T : Any> : ConditionalGenericConverter, ResolvableTypeProvider {
  fun convertContext(context: BotNodeContext): T?

  override fun getResolvableType(): ResolvableType {
    return ResolvableType.forType(Any::class.java)
  }

  override fun getConvertibleTypes(): MutableSet<GenericConverter.ConvertiblePair>? {
    return mutableSetOf(GenericConverter.ConvertiblePair(BotNodeContext::class.java, resolvableType.toClass()))
  }

  override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
    if (source !is BotNodeContext || !resolvableType.isAssignableFrom(targetType.resolvableType)) return null
    return convertContext(source)
  }

  override fun matches(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean {
    return botNodeContextType.isAssignableFrom(sourceType.resolvableType)
      && resolvableType.isAssignableFrom(targetType.resolvableType)
  }
}

@Suppress("UNCHECKED_CAST")
fun List<BotNodeContextConverter<*>>.buildDisjunction(runCatching: Boolean = true): BotNodeContextConverter<Any> {
  return when (size) {
    0 -> NoopBotNodeUpdateConverter as BotNodeContextConverter<Any>
    1 -> first() as BotNodeContextConverter<Any>
    else -> BotNodeContextConverter { context ->
      asSequence().map {
        val result = runCatching {
          it.convertContext(context)
        }
        if (runCatching) {
          result.getOrNull()
        } else {
          result.getOrThrow()
        }
      }.firstOrNull { it != null }
    }
  }
}

internal val botNodeContextType = ResolvableType.forType(BotNodeContext::class.java)