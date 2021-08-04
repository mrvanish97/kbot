package io.github.mrvanish97.kbot.security

import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class CachingBodyHttpRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

  companion object {
    private val UPDATER = AtomicReferenceFieldUpdater.newUpdater(
      CachingBodyHttpRequest::class.java,
      ByteArray::class.java,
      CachingBodyHttpRequest::cachedBytes.name
    )
  }

  @Volatile
  private var cachedBytes: ByteArray? = null

  override fun getInputStream(): ServletInputStream {
    UPDATER.compareAndSet(this, null, super.getInputStream().readAllBytes())
    val wrapped = ByteArrayInputStream(cachedBytes ?: byteArrayOf())
    return WrappedServletInputStream(wrapped)
  }
}