package io.github.mrvanish97.kbot.utils

interface Cleanable {

  fun clean()

  val isCleaned: Boolean

  fun cleanIfNeeded() {
    if (!isCleaned) clean()
  }

}