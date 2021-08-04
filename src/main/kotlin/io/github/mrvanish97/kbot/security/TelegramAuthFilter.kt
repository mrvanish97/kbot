package io.github.mrvanish97.kbot.security

import io.github.mrvanish97.kbot.config.BotPathEncoder
import org.springframework.beans.factory.InitializingBean
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class TelegramAuthFilter(
  private val authenticationManager: AuthenticationManager,
  private val botPathEncoder: BotPathEncoder
) : OncePerRequestFilter(), InitializingBean {

  private val authConverter = TelegramAuthConverter(botPathEncoder)

  override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
    val context = SecurityContextHolder.getContext()
    var requestToUse = request
    val currentAuthentication = context.authentication
    if (currentAuthentication == null || currentAuthentication is AnonymousAuthenticationToken) {
      val cachedRequest = CachingBodyHttpRequest(request)
      val authRequest = authConverter.convert(cachedRequest)
      context.authentication = authenticationManager.authenticate(authRequest)
      val newUrl = botPathEncoder.wipeOffToken(request.requestURI)
      requestToUse = NoQueryStringHttpRequest(cachedRequest)
      if (newUrl != request.requestURI) {
        request.getRequestDispatcher(newUrl).forward(requestToUse, response)
        return
      }
    }
    filterChain.doFilter(requestToUse, response)
  }

}

