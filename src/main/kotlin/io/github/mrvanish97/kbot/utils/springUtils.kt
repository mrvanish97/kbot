package io.github.mrvanish97.kbot.utils

import io.github.mrvanish97.kbnsext.javaName
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.AbstractConfiguredSecurityBuilder
import org.springframework.security.config.annotation.SecurityBuilder
import org.springframework.security.config.annotation.SecurityConfigurerAdapter

fun <O, B : SecurityBuilder<O>, C : SecurityConfigurerAdapter<O, B>> AbstractConfiguredSecurityBuilder<O, B>.apply(
  configurer: C, customizer: Customizer<C>
) {
  customizer.customize(getConfigurer(configurer::class.java) ?: configurer)
}

inline fun <reified A : Annotation> annotationAttributes(
  introspectedClass: Class<*>,
  classValuesAsString: Boolean = true
) = AnnotationAttributes.fromMap(
  AnnotationMetadata.introspect(introspectedClass).getAnnotationAttributes(
    A::class.javaName,
    classValuesAsString
  )
)