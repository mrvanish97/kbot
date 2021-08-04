package io.github.mrvanish97.kbot

import io.github.mrvanish97.kbnsext.javaName
import io.github.mrvanish97.kbot.config.BotNameProvider
import io.github.mrvanish97.kbot.config.BotProperties
import io.github.mrvanish97.kbot.config.WebhookPathTokenProvider
import io.github.mrvanish97.kbot.config.defaultBotName
import io.github.mrvanish97.kbot.controller.annotations.*
import io.github.mrvanish97.kbot.controller.convertSendResult
import io.github.mrvanish97.kbot.controller.node.*
import io.github.mrvanish97.kbot.security.BotAuthenticationProvider
import io.github.mrvanish97.kbot.utils.CompileAsOpen
import io.github.mrvanish97.kbot.utils.KeyExtractorComparator
import io.github.mrvanish97.kbot.utils.annotationAttributes
import io.github.mrvanish97.kbot.utils.selectMethods
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.reactor.mono
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.KotlinDetector
import org.springframework.core.MethodParameter
import org.springframework.core.Ordered
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.OrderUtils
import org.springframework.core.convert.ConversionService
import org.springframework.data.util.NullableUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.util.ClassUtils
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.starter.SpringWebhookBot
import reactor.core.publisher.BaseSubscriber
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter
import java.util.stream.Stream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.kotlinFunction

private val logger = LoggerFactory.getLogger(SpringWebhookBotEx::class.java)

private val defaultAuthProvider = BotAuthenticationProvider { auth -> auth.apply { isAuthenticated = true } }

typealias SpringWebhookBotExFactory = (BotProperties) -> SpringWebhookBotEx

private val contextProperties = BotNodeContext::class.declaredMemberProperties.associateBy {
  it.returnType.javaType as? Class<*>
}

private fun tryToInstantiate(
  outerContext: OuterContext,
  requestedClass: Class<*>,
  paramIndex: Int,
  method: Method
): Any {
  val param = method.parameters[paramIndex]
  val ctor = BeanUtils.getResolvableConstructor(requestedClass)
  if (!ctor.trySetAccessible()) {
    throw IllegalAccessException(
      "Cannot set accessible constructor $ctor for binding outer context with requested class ${requestedClass.name}"
    )
  }
  return if (ctor.parameterCount == 0) {
    BeanUtils.instantiateClass(ctor)
  } else {
    val arguments = ctor.parameters.map {
      outerContext[it.type] ?: IllegalArgumentException("Cannot bind value for $it while binding $param in $method")
    }.toTypedArray()
    BeanUtils.instantiateClass(ctor, *arguments)
  }
}

private fun seedMapper(index: Int, seed: ParameterValueSeed, context: OuterContext, method: Method): Any? {
  return when (seed) {
    is ReadyParameterValueSeed<*> -> seed.value
    is OuterContextParameterValueSeed<*> -> tryToInstantiate(context, seed.requestedClass, index, method)
  }
}

private val iterableType = ResolvableType.forClassWithGenerics(Iterable::class.java, PartialBotApiMethod::class.java)
private val iteratorType = ResolvableType.forClassWithGenerics(Iterator::class.java, PartialBotApiMethod::class.java)
private val sequenceType = ResolvableType.forClassWithGenerics(Sequence::class.java, PartialBotApiMethod::class.java)
private val streamType = ResolvableType.forClassWithGenerics(Stream::class.java, PartialBotApiMethod::class.java)

private val botNodeDescriptorComparator = KeyExtractorComparator<BotNodeDescriptor>().make {
  OrderUtils.getOrder(it.beanType, Ordered.LOWEST_PRECEDENCE)
}.thenBy {
  OrderUtils.getOrder(it.method) ?: Ordered.LOWEST_PRECEDENCE
}

private val alwaysTrueSingleton = { _: Any -> true }

@CompileAsOpen
class SpringWebhookBotEx @Autowired constructor(
  defaultBotOptions: DefaultBotOptions,
  setWebhook: SetWebhook,
  private val botProperties: BotProperties,
  private val encodedBotPath: String,
  private val applicationContext: ApplicationContext,
  private val conversionService: ConversionService
) : SpringWebhookBot(defaultBotOptions, setWebhook),
  WebhookPathTokenProvider,
  BotNameProvider, CoroutineServletWebhookBot {

  companion object {
    const val DEFAULT_FACTORY_BEAN_NAME =
      "io.github.mrvanish97.kbot.api.SpringWebhookBotEx.springWebhookBotExFactoryBean"
  }

  private val isDefault = applicationContext.bots.defaultBotName == botProperties.botName

  private val botNodes = applicationContext.getBotBeansWithAnnotation<Controller>(botProperties.botName)
    .asSequence()
    .map { entry ->
      val bean = entry.value
      val userType = ClassUtils.getUserClass(bean)
      userType.selectMethods {
        selectMethodForBotNodeInfo(it, bean, userType)
      }.map {
        BotNodeDescriptorImpl(bean, userType, it.key, it.value)
      }
    }.flatten().sortedWith(botNodeDescriptorComparator).toList()

  private fun selectMethodForBotNodeInfo(method: Method, bean: Any, type: Class<*>): BotNodeProperties? {
    if (!Modifier.isPublic(method.modifiers)) return null
    val botNode = AnnotatedElementUtils.getMergedAnnotationAttributes(method, BotNode::class.java)
      ?: return null
    val botNodeName = botNode.getString(BotNode::value.name).takeIf { it.isNotBlank() }
      ?: ClassUtils.getQualifiedMethodName(method, bean::class.java)
    val numberOfParametersToTake = if (KotlinDetector.isSuspendingFunction(method)) {
      method.parameterCount - 1
    } else {
      method.parameterCount
    }
    val kotlinParameters = method.kotlinFunction?.parameters
    val parametersInfo = method.parameters.asSequence()
      .take(numberOfParametersToTake)
      .mapIndexed { index, parameter ->
        buildBotNodeParameterInfo(parameter, index, method, botNodeName, bean, type, kotlinParameters)
      }.toList()
    return BotNodePropertiesImpl(
      botNodeName = botNodeName,
      parametersInfo = parametersInfo,
      isExecutableCallback = buildBotNodeExecutionPredicate(bean, type, method, botNodeName)
    )
  }

  private fun buildBotNodeExecutionPredicate(
    bean: Any,
    type: Class<*>,
    method: Method,
    botNodeName: String
  ): BotNodeExecutionPredicate {
    val methodDescriptor = BotNodeMethodDescriptorImpl(
      controllerBean = bean,
      controllerType = type,
      method = method,
      botNodeName = botNodeName
    )
    return when (val lookupResult = lookupBotNodeAspect<ExecuteIf>(method, applicationContext)) {
      is BeanLookupResult -> lookupResult.bean as? BotNodeExecutionPredicate
        ?: throw IllegalArgumentException(
          "${lookupResult.bean} is not instance of ${BotNodeExecutionPredicate::class.javaName}"
        )
      is FactoryLookupResult -> {
        val factory = lookupResult.factory as? BotNodeExecutionPredicateFactory
          ?: throw  IllegalArgumentException(
            "${lookupResult.factory} is not instance of ${BotNodeExecutionPredicateFactory::class.javaName}"
          )
        BotNodeExecutionPredicate { factory.build(methodDescriptor).isExecutable(it) }
      }
      EmptyLookupResult -> {
        // I'm lazy to do that, need to refactor this
        BotNodeExecutionPredicate { false }
      }
    }
  }

  private fun buildBotNodeParameterInfo(
    param: Parameter,
    paramIndex: Int,
    method: Method,
    botNodeName: String,
    bean: Any,
    type: Class<*>,
    kotlinParameters: List<KParameter>?,
  ): BotNodeParameterInfo<*> {
    val isNullable = if (kotlinParameters != null) {
      kotlinParameters[paramIndex].type.isMarkedNullable
    } else {
      NullableUtils.isExplicitNullable(MethodParameter(method, paramIndex))
    }
    return if (AnnotatedElementUtils.hasAnnotation(param, FromContext::class.java)) {
      val converter = buildConverterFromParameter(param, method, botNodeName, bean, type)
      PlainBotNodeParameterInfo(converter, isNullable)
    } else {
      val contextProperty = contextProperties[param.type]
      if (contextProperty != null) {
        PlainBotNodeParameterInfo({
          contextProperty(it)
        }, isNullable)
      } else {
        OuterContextParameterValueSeed(requestedClass = param.type, isRequired = !isNullable)
      }
    }
  }

  private fun findConvertersInContext(parameterType: ResolvableType): List<BotNodeContextConverter<*>> {
    return applicationContext.getBeansOfType(BotNodeContextConverter::class.java).values.filter {
      if (!parameterType.isAssignableFrom(it.resolvableType)) return@filter false
      val botAttributes = annotationAttributes<Bot>(it::class.java)
      if (botAttributes != null) {
        val botNameFromBotAttributes = botAttributes.getString(Bot::value.name)
        botNameFromBotAttributes == botProperties.botName || isDefault && botNameFromBotAttributes.isBlank()
      } else {
        true
      }
    }
  }

  private fun findConverterFactoriesInContext(): List<BotNodeContextConverterFactory<*>> {
    return applicationContext.getBeansOfType(BotNodeContextConverterFactory::class.java).values.filter {
      val botAttributes = annotationAttributes<Bot>(it::class.java)
      if (botAttributes != null) {
        val botNameFromBotAttributes = botAttributes.getString(Bot::value.name)
        botNameFromBotAttributes == botProperties.botName || isDefault && botNameFromBotAttributes.isBlank()
      } else {
        true
      }
    }
  }

  private fun buildConverterFromParameter(
    param: Parameter,
    method: Method,
    botNodeName: String,
    bean: Any,
    type: Class<*>
  ): BotNodeContextConverter<*> {

    val lookupResult = lookupBotNodeAspect<FromContext>(param, applicationContext)

    val extraContextData = lazy { BotNodeParameterDescriptorImpl(method, param, botNodeName, bean, type) }

    return when (lookupResult) {
      is BeanLookupResult -> lookupResult.bean as? BotNodeContextConverter<*>
        ?: throw IllegalArgumentException(
          "${lookupResult.bean} is not instance of ${BotNodeContextConverter::class.javaName}"
        )
      is FactoryLookupResult -> {
        val factory = lookupResult.factory as? BotNodeContextConverterFactory<*>
          ?: throw  IllegalArgumentException(
            "${lookupResult.factory} is not instance of ${BotNodeContextConverterFactory::class.javaName}"
          )
        factory.build(extraContextData.value)
      }
      EmptyLookupResult -> {
        // a really desperate way to obtain something from the application context
        val converters = findConvertersInContext(ResolvableType.forMethodParameter(MethodParameter.forParameter(param)))
        val builtConverters = findConverterFactoriesInContext().map { it.build(extraContextData.value) }

        return converters.plus(builtConverters).buildDisjunction()
      }
    }
  }

  override fun getBotToken() = botProperties.telegramToken

  override fun getBotUsername() = botProperties.username

  override fun onWebhookUpdateReceived(update: Update): BotApiMethod<*>? {
    return null
  }

  override fun getBotPath() = encodedBotPath

  override val pathToken: CharSequence
    get() = botProperties.webhookToken

  override val botName: String
    get() = botProperties.botName

  private val botNodeInvocationComparator = KeyExtractorComparator<BotNodeInvocation>().make {
    OrderUtils.getOrder(it.first.method) ?: Ordered.LOWEST_PRECEDENCE
  }

  private fun getBodNodeInvocationByContext(context: BotNodeContext) = botNodes.asSequence()
    .filter {
      it.properties.isExecutable(context)
    }.mapNotNull { descriptor ->
      val parameterSeeds = descriptor.properties.parametersInfo.map {
        when (it) {
          is PlainBotNodeParameterInfo<*> -> {
            val converted = it.converter.convertContext(context)
            if (converted == null && it.isRequired) {
              return@mapNotNull null
            }
            ReadyParameterValueSeed(converted)
          }
          is OuterContextParameterValueSeed<*> -> it
        }
      }
      BotNodeInvocation(descriptor, parameterSeeds)
    }.maxWithOrNull(botNodeInvocationComparator)

  // "inspired" by CoroutinesUtils:invokeSuspendingFunction
  private fun invokeSuspended(
    botNodeDescriptor: BotNodeDescriptor,
    parameterValueSeeds: List<ParameterValueSeed>,
    coroutineContextFactory: () -> CoroutineContext,
    genericOuterContext: OuterContext
  ): Publisher<*> {
    val kotlinFunction = botNodeDescriptor.method.kotlinFunction!!
    val mono = mono(coroutineContextFactory()) {
      val outerContext = listOf(
        typePair(this),
        typePair(coroutineContext),
      ).plus(genericOuterContext)
      val parameterValuesList = parameterValueSeeds.mapIndexed { index, seed ->
        seedMapper(index, seed, outerContext, botNodeDescriptor.method)
      }.toList()
      val fullParameterValues = listOf(botNodeDescriptor.bean).plus(parameterValuesList).toTypedArray()
      kotlinFunction.callSuspend(*fullParameterValues).takeUnless { it is Unit }
    }.onErrorMap(InvocationTargetException::class.java) { it.targetException }
    val classifier = kotlinFunction.returnType.classifier as? KClass<*>
    return if (classifier != null && Flow::class.java.isAssignableFrom(classifier.java)) {
      mono.flatMapMany {
        @Suppress("UNCHECKED_CAST")
        (it as? Flow<Any>)?.asFlux()
      }
    } else {
      mono
    }
  }

  private fun invokePlain(
    botNodeDescriptor: BotNodeDescriptor,
    parameterValueSeeds: List<ParameterValueSeed>,
    genericOuterContext: OuterContext
  ): Any? {
    val parameterValues = parameterValueSeeds.mapIndexed { index, seed ->
      seedMapper(index, seed, genericOuterContext, botNodeDescriptor.method)
    }.toTypedArray()
    return botNodeDescriptor.method.invoke(botNodeDescriptor.bean, *parameterValues)
  }

  private inner class ResultSubscriber(
    private val update: Update,
    private val botNodeDescriptor: BotNodeDescriptor
  ) : BaseSubscriber<Any?>() {

    override fun hookOnNext(value: Any) {
      if (value is Publisher<*>) {
        value.subscribe(ResultSubscriber(update, botNodeDescriptor))
      } else {
        handleResult(update, value)
      }
    }

    override fun hookOnError(throwable: Throwable) {
      handleThrowable(botNodeDescriptor, update, throwable)
    }

  }

  override suspend fun onWebhookUpdateReceived(
    update: Update,
    request: HttpServletRequest,
    response: HttpServletResponse,
    coroutineContextFactory: () -> CoroutineContext
  ) {
    val context = BotNodeContextImpl(
      sender = this,
      update = update,
      request = request,
      response = response,
      auth = SecurityContextHolder.getContext().authentication
    )
    val outerContext = contextProperties.mapNotNull {
      val contextObject = it.value(context) ?: return@mapNotNull null
      typePair(contextObject)
    }.plus(typePair(context))
    val (botNodeDescriptor, parameterSeeds) = getBodNodeInvocationByContext(context) ?: return
    val result = runCatching {
      if (KotlinDetector.isSuspendingFunction(botNodeDescriptor.method)) {
        invokeSuspended(botNodeDescriptor, parameterSeeds, coroutineContextFactory, outerContext)
      } else {
        invokePlain(botNodeDescriptor, parameterSeeds, outerContext)
      }
    }.getOrElse {
      handleThrowable(botNodeDescriptor, update, it)
      null
    } ?: return
    if (result is Publisher<*>) {
      result.subscribe(ResultSubscriber(update, botNodeDescriptor))
    } else {
      handleResult(update, result)
    }
  }

  private fun handleThrowable(descriptor: BotNodeDescriptor, update: Update, throwable: Throwable) {
    // nothing yet
  }

  private val sendMessageScope = CoroutineScope(Dispatchers.IO)

  private fun handleResult(update: Update, result: Any) {
    val resultType = ResolvableType.forType(result::class.java)
    val methodsIterator = if (result is PartialBotApiMethod<*>) {
      sequenceOf(result).iterator()
    } else {
      @Suppress("UNCHECKED_CAST")
      when (resultType) {
        iteratorType::isAssignableFromType -> result as Iterator<PartialBotApiMethod<*>>
        iterableType::isAssignableFromType -> (result as? Iterable<PartialBotApiMethod<*>>)?.iterator()
        sequenceType::isAssignableFromType -> (result as? Sequence<PartialBotApiMethod<*>>)?.iterator()
        streamType::isAssignableFromType -> (result as? Stream<PartialBotApiMethod<*>>)?.iterator()
        else -> conversionService.convertSendResult(update, result)?.iterator()
      } ?: return
    }
    launchSendMessages(methodsIterator)
  }

  private fun launchSendMessages(iterator: Iterator<PartialBotApiMethod<*>>) {
    if (!iterator.hasNext()) {
      return
    }
    sendMessageScope.launch {
      iterator.forEach {
        execute(it)
      }
    }
  }

}

private fun ResolvableType.isAssignableFromType(other: ResolvableType) = isAssignableFrom(other)