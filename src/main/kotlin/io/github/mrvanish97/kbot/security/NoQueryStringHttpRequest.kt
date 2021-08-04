package io.github.mrvanish97.kbot.security

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class NoQueryStringHttpRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

  override fun getQueryString(): String? {
    return null
  }

}