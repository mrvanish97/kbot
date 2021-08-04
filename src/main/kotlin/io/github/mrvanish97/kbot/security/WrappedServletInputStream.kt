package io.github.mrvanish97.kbot.security

import java.io.InputStream
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream

class WrappedServletInputStream(private val inputStream: InputStream) : ServletInputStream() {

  companion object {
    private val UPDATER = AtomicIntegerFieldUpdater
      .newUpdater(WrappedServletInputStream::class.java, WrappedServletInputStream::lastValue.name)
  }

  private val listeners = CopyOnWriteArrayList<ReadListener>()

  @Volatile
  private var lastValue = Int.MAX_VALUE

  private fun reportException(t: Throwable) {
    listeners.forEach { it.onError(t) }
  }

  override fun read(): Int {
    return runCatching {
      inputStream.read().also {
        updateLastValue(it)
      }
    }.onFailure(this::reportException).getOrThrow()
  }

  override fun read(b: ByteArray, off: Int, len: Int): Int {
    return runCatching {
      inputStream.read(b, off, len).also {
        if (it < len) {
          updateLastValue(-1)
        }
      }
    }.onFailure(this::reportException).getOrThrow()
  }

  private fun updateLastValue(value: Int) {
    UPDATER.set(this, value)
    if (value == -1) {
      listeners.forEach { it.onAllDataRead() }
    }
  }

  override fun isFinished(): Boolean {
    return UPDATER.compareAndSet(this, -1, -1)
  }

  override fun isReady() = true

  override fun setReadListener(listener: ReadListener) {
    listener.onDataAvailable()
    listeners.addIfAbsent(listener)
  }

}