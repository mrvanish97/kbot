package io.github.mrvanish97.kbot.utils

import java.util.concurrent.atomic.AtomicBoolean

class CleanableString(private val value: CharArray) : CharSequence, Cleanable, Cloneable {

  companion object {
    @JvmField
    val EMPTY = CleanableString(charArrayOf()).apply { clean() }
  }

  private val cleaned = AtomicBoolean(false)

  override val length: Int
    get() {
      checkCleaned()
      return value.size
    }

  override fun get(index: Int): Char {
    checkCleaned()
    checkBounds(index)
    return value[index]
  }

  override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
    checkCleaned()
    checkBounds(startIndex)
    checkBounds(endIndex)
    return CleanableString(value.copyOfRange(startIndex, endIndex))
  }

  override fun clean() {
    checkCleaned()
    this.cleaned.set(true)
    value.fill('\u0000')
  }

  override val isCleaned: Boolean
    get() = this.cleaned.compareAndSet(true, true)

  private fun checkCleaned() {
    if (this.isCleaned) {
      throw IllegalArgumentException("CleanableString is already cleaned")
    }
  }

  private fun checkBounds(index: Int) {
    if (index < 0 || index >= value.size) {
      throw IllegalArgumentException("Index $index is out of bounds")
    }
  }

  override fun toString(): String {
    checkCleaned()
    return String(value)
  }

  public override fun clone(): Any {
    return value.copyOf()
  }

}