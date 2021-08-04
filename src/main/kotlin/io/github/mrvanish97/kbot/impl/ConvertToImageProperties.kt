package io.github.mrvanish97.kbot.impl

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.validation.annotation.Validated
import java.nio.file.Path

@ConfigurationProperties("neon-fonts.convert-to-image")
@Validated
@ConstructorBinding
data class ConvertToImageProperties(
  val maxHeight: Int,
  val maxRows: Int,
  val maxColumns: Int,
  val padding: Int,
  val fontsPath: Path,
  val bgPath: Path,
  val noFontsMessage: String
)