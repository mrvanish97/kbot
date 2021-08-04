package io.github.mrvanish97.kbot.utils

import org.springframework.context.annotation.PropertySource
import org.springframework.core.annotation.AliasFor

@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@PropertySource(factory = YamlPropertySourceFactory::class)
annotation class YamlPropertySource(
  @get:AliasFor("value", annotation = PropertySource::class)
  vararg val value: String,

  @get:AliasFor("name", annotation = PropertySource::class)
  val name: String = "",

  @get:AliasFor("ignoreResourceNotFound", annotation = PropertySource::class)
  val ignoreResourceNotFound: Boolean = false,

  @get:AliasFor("encoding", annotation = PropertySource::class)
  val encoding: String = ""
)
