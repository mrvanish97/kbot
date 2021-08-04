package io.github.mrvanish97.kbot.controller.node

internal typealias TypePair<T> = Pair<Class<out T>, T>

internal inline fun <reified T : Any> typePair(value: T) = TypePair(T::class.java, value)