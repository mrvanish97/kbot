package io.github.mrvanish97.kbot.config

import io.github.mrvanish97.kbot.utils.YamlPropertySource

@Target(AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@YamlPropertySource("classpath:/kbot/bots.yml", name="bots")
annotation class BotsPropertySource
