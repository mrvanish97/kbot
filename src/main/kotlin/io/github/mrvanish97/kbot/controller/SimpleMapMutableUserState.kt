package io.github.mrvanish97.kbot.controller

class SimpleMapMutableUserState<T>(
  private val map: MutableMap<Long, T>,
  private val getDefaultValue: (Long) -> T
) : MutableUserState<T> {

  override fun get(userId: Long): T {
    return map[userId] ?: getDefaultValue(userId)
  }

  override fun set(userId: Long, value: T) {
    map[userId] = value
  }

}