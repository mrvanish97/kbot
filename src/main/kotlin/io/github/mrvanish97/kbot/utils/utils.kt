package io.github.mrvanish97.kbot.utils

import java.security.SecureRandom
import java.util.*

fun String.containsCyrillic(): Boolean {
  return any {
    Character.UnicodeBlock.of(it).equals(Character.UnicodeBlock.CYRILLIC)
  }
}

fun <K, V> Iterable<Pair<K, V>>.toMultiMap(): Map<K, List<V>> {
  val map = mutableMapOf<K, MutableList<V>>()
  for ((key, value) in this) {
    map.computeIfAbsent(key) { mutableListOf() }.add(value)
  }
  return map
}

private val random = SecureRandom()

fun generateToken(length: Int): String {
  if (length < 1) {
    throw IllegalArgumentException("The length of the generating token must be positive")
  }
  val bytes = ByteArray(length)
  random.nextBytes(bytes)
  return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

fun <T> Optional<T>.asNullable(): T? = orElse(null)

class KeyExtractorComparator<T> {
  fun <C : Comparable<C>> make(keyExtractor: (T) -> C) = Comparator.comparing(keyExtractor)
}