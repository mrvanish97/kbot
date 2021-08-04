package io.github.mrvanish97.kbot.controller.node

import org.springframework.core.ResolvableType
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.TypeDescriptor

class ConversionServiceBotNodeContextConverter(
  private val conversionService: ConversionService,
  private val botNodeTypeDescriptor: TypeDescriptor,
  private val parameterTypeDescriptor: TypeDescriptor
) : BotNodeContextConverter<Any> {
  override fun convertContext(context: BotNodeContext): Any? {
    return conversionService.convert(context, botNodeTypeDescriptor, parameterTypeDescriptor)
  }

  override fun getResolvableType(): ResolvableType {
    return parameterTypeDescriptor.resolvableType
  }

}