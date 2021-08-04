package io.github.mrvanish97.kbot.config

import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class WipeTokenHandlerInterceptor(private val botPathEncoder: BotPathEncoder) : HandlerInterceptor {

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    return super.preHandle(request, response, handler)
  }
}