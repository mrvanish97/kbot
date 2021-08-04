package io.github.mrvanish97.kbot.security

import io.github.mrvanish97.kbot.config.BotPathEncoder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class BotSecurityConfig @Autowired constructor(
  private val telegramAuthProvider: TelegramAuthProvider,
  private val botPathEncoder: BotPathEncoder
) : WebSecurityConfigurerAdapter() {

  override fun configure(auth: AuthenticationManagerBuilder) {
    auth.authenticationProvider(telegramAuthProvider)
  }

  override fun configure(http: HttpSecurity) {
    http.httpBasic {
      it.disable()
    }.formLogin {
      it.disable()
    }.csrf {
      it.disable()
    }.anonymous {
      it.disable()
    }.exceptionHandling {
      it.authenticationEntryPoint { _, response, _ ->
        response.status = HttpStatus.NOT_FOUND.value()
      }
      it.accessDeniedHandler { _, response, _ ->
        response.status = HttpStatus.NOT_FOUND.value()
      }
    }
    http.apply(TelegramAuthConfigurer(botPathEncoder))
  }

}