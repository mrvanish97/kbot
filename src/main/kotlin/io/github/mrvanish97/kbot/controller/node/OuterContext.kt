package io.github.mrvanish97.kbot.controller.node

internal typealias OuterContext = List<TypePair<out Any>>

internal operator fun OuterContext.get(requestedClass: Class<*>): Any? {
  return find {
    requestedClass.isAssignableFrom(it.first)
  }?.second
}

internal operator fun OuterContext.get(seed: OuterContextParameterValueSeed<*>) = get(seed.requestedClass)