package io.github.mrvanish97.kbot.controller.node

import io.github.mrvanish97.kbot.controller.annotations.EvaluationContextVariablesResolver
import io.github.mrvanish97.kbot.controller.annotations.ExpressionConverter
import io.github.mrvanish97.kbot.controller.annotations.buildVariablesResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component

@Component
class ExpressionConverterFactory @Autowired constructor(
  private val context: ApplicationContext
) : BotNodeContextConverterFactory<Any> {

  private val parser = SpelExpressionParser()

  private fun buildEvalContext() = StandardEvaluationContext().apply {
    setBeanResolver(BeanFactoryResolver(context))
  }

  private fun buildExpressionBasedContextConverter(
    expression: String,
    resultClass: Class<*>,
    resolver: EvaluationContextVariablesResolver
  ): BotNodeContextConverter<*> {
    val parsedExpression = parser.parseExpression(expression)
    return BotNodeContextConverter {
      val evaluationContext = buildEvalContext()
      evaluationContext.setRootObject(it)
      resolver.processVariables(evaluationContext)
      parsedExpression.getValue(evaluationContext, it, resultClass)
    }
  }

  override fun build(parameter: BotNodeParameterDescriptor): BotNodeContextConverter<Any> {
    val param = parameter.parameter
    val resolver = buildVariablesResolver(param)
    val expressions = AnnotatedElementUtils
      .findAllMergedAnnotations(param, ExpressionConverter::class.java)
      .map {
        it.expressions.toList()
      }.flatten()
    return expressions.map {
      buildExpressionBasedContextConverter(it, param.type, resolver)
    }.buildDisjunction()
  }
}