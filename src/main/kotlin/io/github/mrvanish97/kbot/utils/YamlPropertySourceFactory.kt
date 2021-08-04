package io.github.mrvanish97.kbot.utils

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory
import java.io.IOException


class YamlPropertySourceFactory : PropertySourceFactory {
  override fun createPropertySource(name: String?, resource: EncodedResource): PropertySource<*> {
    val factory = YamlPropertiesFactoryBean()
    factory.setResources(resource.resource)
    val properties = factory.`object` ?: throw IOException()
    val filename = resource.resource.filename ?: throw IOException()
    return PropertiesPropertySource(filename, properties)
  }
}