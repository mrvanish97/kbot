package io.github.mrvanish97.kbot.controller.annotations

import com.google.common.graph.GraphBuilder
import com.google.common.graph.Graphs
import com.google.common.graph.Traverser
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.expression.EvaluationContext
import org.springframework.expression.Expression
import org.springframework.expression.spel.SpelNode
import org.springframework.expression.spel.ast.VariableReference
import org.springframework.expression.spel.standard.SpelExpression
import org.springframework.expression.spel.standard.SpelExpressionParser
import java.lang.reflect.AnnotatedElement
import java.util.concurrent.ConcurrentHashMap

private const val THIS_NAME = "this"
private const val ROOT_NAME = "root"

private val GLOBAL_VARIABLE_NAMES = arrayOf(THIS_NAME, ROOT_NAME)

data class PreparedVariableDefinition(
  val variableName: String,
  val expression: Expression,
  val dependsOn: List<String>
)

interface RankedDefinition{
  val variableName: String
  val expression: Expression
}

internal interface RankedMutableDefinition : RankedDefinition {
  fun add(dependency: RankedDefinition)
}

internal data class RankedMutableDefinitionSeed(
  override val variableName: String,
  override val expression: Expression,
  val dependsOn: MutableList<RankedMutableDefinitionSeed>
) : RankedMutableDefinition {
  override fun add(dependency: RankedDefinition) {
    if (dependency !is RankedMutableDefinitionSeed)
      throw UnsupportedOperationException("Only instances of RankedMutableDefinitionImpl are supported")
    dependsOn.add(dependency)
  }
}

private val parser = SpelExpressionParser()

class SpelNodeDfsIterator(private val start: SpelNode) : Iterator<SpelNode> {

  private val stack = ArrayDeque<SpelNode>().apply { add(start) }

  override fun hasNext() = stack.isNotEmpty()

  override fun next(): SpelNode {
    val node = stack.removeLast()
    for (i in node.childCount - 1 downTo 0) {
      stack.add(node.getChild(i))
    }
    return node
  }

}

fun SpelExpression.listAllNodes() = Sequence { SpelNodeDfsIterator(ast) }

private val preparedVariableDefinitionCache = ConcurrentHashMap<String, Pair<Expression, List<String>>>()

private fun prepareVariableDefinitions(
  variableResolvers: Iterable<VariableResolver>
): Iterable<PreparedVariableDefinition> {
  return variableResolvers.asSequence().map { resolver ->
    val (expression, dependsOn) = preparedVariableDefinitionCache.getOrPut(resolver.expression) {
      val expression = parser.parseRaw(resolver.expression)
      val dependsOn = expression.listAllNodes()
        .filterIsInstance<VariableReference>()
        .map { it.toStringAST().substringAfter('#') }
        .filterNot { GLOBAL_VARIABLE_NAMES.contains(it) }
        .toList()
      Pair(expression, dependsOn)
    }
    PreparedVariableDefinition(resolver.name, expression, dependsOn)
  }.asIterable()
}

interface EvaluationContextVariablesResolver {

  fun processVariables(context: EvaluationContext)

}

class RankedDefinitionResolver(
  private val rankedDefinitions: List<RankedDefinition>
) : EvaluationContextVariablesResolver {

  override fun processVariables(context: EvaluationContext) {
    rankedDefinitions.forEach { def ->
      context.setVariable(def.variableName, def.expression.getValue(context))
    }
  }

}

private val rankedDefinitionsCache = ConcurrentHashMap<Set<PreparedVariableDefinition>, List<RankedDefinition>>()

private fun getOrCreateRankedDependencyFromCache(
  def: PreparedVariableDefinition,
  dependencyCache: MutableMap<String, RankedMutableDefinitionSeed>
): RankedMutableDefinitionSeed {
  with(def) {
    return dependencyCache[variableName] ?: RankedMutableDefinitionSeed(
      variableName = variableName,
      expression = expression,
      dependsOn = ArrayList(dependsOn.size)
    ).also {
      dependencyCache[variableName] = it
    }
  }
}

private fun prepareSetWithDefinitions(
  preparedVariableDefinitions: Iterable<PreparedVariableDefinition>
): MutableSet<PreparedVariableDefinition> {
  val defNames = hashSetOf<String>()
  val preparedDefinitionsList = preparedVariableDefinitions.onEach {
    if (defNames.contains(it.variableName)) {
      throw IllegalArgumentException(
        "Duplicated variable '${it.variableName}' detected while processing VariableResolvers. Variable: $it"
      )
    } else {
      defNames.add(it.variableName)
    }
  }.toList()
  val preparedDefinitionsSet = HashSet<PreparedVariableDefinition>(preparedDefinitionsList.size)
  val preparedDefinitionsMap = HashMap<String, PreparedVariableDefinition>(preparedDefinitionsList.size)
  for (i in preparedDefinitionsList.indices) {
    val def = preparedDefinitionsList[i]
    preparedDefinitionsSet.add(def)
    preparedDefinitionsMap[def.variableName] = def
  }
  return preparedDefinitionsSet
}

private fun makeNotReadyRankedDefinition(
  def: PreparedVariableDefinition,
  cache: MutableMap<String, RankedMutableDefinitionSeed>,
  preparedDefinitionsMap: Map<String, PreparedVariableDefinition>
): RankedMutableDefinitionSeed {
  val rankedDef = getOrCreateRankedDependencyFromCache(def, cache)
  for (j in def.dependsOn.indices) {
    val dependencyName = def.dependsOn[j]
    if (dependencyName == rankedDef.variableName) {
      throw IllegalArgumentException("Variable expression cannot refer to itself. Variable: $def")
    }
    val dependency = preparedDefinitionsMap[dependencyName] ?: continue
    val rankedDependency = getOrCreateRankedDependencyFromCache(dependency, cache)
    rankedDef.dependsOn.add(rankedDependency)
  }
  return rankedDef
}

@Suppress("UnstableApiUsage")
private fun buildRankedDefinitions(preparedVariableDefinitions: Iterable<PreparedVariableDefinition>): List<RankedDefinition> {
  val preparedDefinitionsSet = prepareSetWithDefinitions(preparedVariableDefinitions)
  return rankedDefinitionsCache.getOrPut(preparedDefinitionsSet) {
    val dependencyCache = hashMapOf<String, RankedMutableDefinitionSeed>()
    val preparedDefinitionsList = preparedDefinitionsSet.toList()
    val preparedDefinitionsMap = preparedDefinitionsList.associateBy { it.variableName }
    val rankedDefs = ArrayList<RankedMutableDefinitionSeed>(preparedDefinitionsList.size)
    repeat(preparedDefinitionsList.size) {
      val def = preparedDefinitionsList[it]
      rankedDefs.add(makeNotReadyRankedDefinition(def, dependencyCache, preparedDefinitionsMap))
    }
    val graph = GraphBuilder.directed().build<RankedMutableDefinitionSeed>()
    repeat(rankedDefs.size) { i ->
      val def = rankedDefs[i]
      graph.addNode(def)
      val dependencies = def.dependsOn
      repeat(dependencies.size) { j ->
        val dependency = dependencies[j]
        graph.addNode(dependency)
        graph.putEdge(dependency, def)
      }
    }
    if (Graphs.hasCycle(graph)) {
      throw IllegalArgumentException("A cyclic dependencies were detected during the processing")
    }
    var minimalDegree = Int.MAX_VALUE
    var starterNodes = mutableListOf<RankedMutableDefinitionSeed>()
    val degrees = hashMapOf<Int, MutableList<RankedMutableDefinitionSeed>>()
    repeat(rankedDefs.size) {
      val def = rankedDefs[it]
      val degree = graph.inDegree(def)
      if (degree < minimalDegree) {
        minimalDegree = degree
        starterNodes = mutableListOf(def)
      } else if (degree == minimalDegree) {
        starterNodes.add(def)
      }
      degrees.getOrPut(degree) { mutableListOf() }.add(def)
    }
    Traverser.forGraph(graph).breadthFirst(starterNodes).toList()
  }
}

fun buildVariablesResolver(element: AnnotatedElement): EvaluationContextVariablesResolver {
  val variableResolvers = AnnotatedElementUtils.getMergedRepeatableAnnotations(element, VariableResolver::class.java)
  val preparedVariableDefinitions = prepareVariableDefinitions(variableResolvers)
  val rankedDefinitions = buildRankedDefinitions(preparedVariableDefinitions)
  return try {
    RankedDefinitionResolver(rankedDefinitions)
  } catch (e: IllegalArgumentException) {
    throw IllegalArgumentException("Cannot build VariableDependencies for $element", e)
  }
}