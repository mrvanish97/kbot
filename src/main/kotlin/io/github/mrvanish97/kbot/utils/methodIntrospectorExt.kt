package io.github.mrvanish97.kbot.utils

import org.springframework.core.MethodIntrospector
import java.lang.reflect.Method

fun <T : Any> Class<*>.selectMethods(lookup: MethodIntrospector.MetadataLookup<T?>): Map<Method, T> {
  @Suppress("UNCHECKED_CAST")
  return MethodIntrospector.selectMethods(this, lookup) as Map<Method, T>
}