package io.github.mrvanish97.kbot.controller.node

import io.github.mrvanish97.kbot.controller.annotations.ExpressionPredicate
import io.github.mrvanish97.kbot.controller.annotations.buildVariablesResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class ExpressionExecutionPredicateFactory @Autowired constructor(
  private val context: ApplicationContext
) : BotNodeExecutionPredicateFactory {

  private val parser = SpelExpressionParser()

  private fun buildEvalContext() = StandardEvaluationContext().apply {
    setBeanResolver(BeanFactoryResolver(context))
  }

  override fun build(parameter: BotNodeMethodDescriptor): BotNodeExecutionPredicate {
    val element = parameter.method
    val resolver = buildVariablesResolver(element)
    val expressions = AnnotatedElementUtils
      .findAllMergedAnnotations(element, ExpressionPredicate::class.java)
      .map {
        it.expressions.toList()
      }.flatten()
    return expressions.map { expression ->
      val parsed = parser.parseExpression(expression)
      BotNodeExecutionPredicate {
        val evalContext = buildEvalContext()
        resolver.processVariables(evalContext)
        parsed.getValue(evalContext, it, Boolean::class.java) == true
      }
    }.buildConjunction()
  }


}