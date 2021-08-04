package io.github.mrvanish97.kbot.impl

import io.github.mrvanish97.kbnsext.annotateWith
import io.github.mrvanish97.kbot.SpringWebhookBotEx.Companion.DEFAULT_FACTORY_BEAN_NAME
import io.github.mrvanish97.kbot.SpringWebhookBotExFactory
import io.github.mrvanish97.kbot.config.Bots
import io.github.mrvanish97.kbot.controller.DefaultUserStateService
import io.github.mrvanish97.kbot.utils.YamlPropertySource

rootConfiguration().annotateWith<YamlPropertySource> {
  it::value set "classpath:/neon-bot/convert-to-image.yml"
}

bean("neonFontsProperties") {
  ref<Bots>()["neon-fonts"]
}

bean("neonFontBot") {
  ref<SpringWebhookBotExFactory>(DEFAULT_FACTORY_BEAN_NAME)(ref("neonFontsProperties"))
}

bean("neonFontBotStateService") {
  DefaultUserStateService()
}