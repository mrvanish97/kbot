package io.github.mrvanish97.kbot.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.nio.file.Path
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@ConditionalOnProperty("bots.use-bundled", matchIfMissing = true)
@ConfigurationProperties
@ConstructorBinding
data class BundledBots(
  private val bots: List<BundledBotProperties>
) : Bots {

  @Validated
  data class BundledBotProperties(
    @NotBlank override val telegramToken: String,
    @NotBlank override val username: String,
    @NotBlank override val url: String,
    @NotBlank override val botName: String,
    @NotBlank override val webhookToken: String,
    override val certificatePath: Path?,
    @NotNull override val initialOwnerId: Long
  ): BotProperties

  private val botsMap by lazy { bots.associateBy { it.botName } }

  override fun get(name: String) = botsMap[name] ?: throw IllegalArgumentException("Could not find the bot $name")

  override val names = bots.map { it.botName }
}