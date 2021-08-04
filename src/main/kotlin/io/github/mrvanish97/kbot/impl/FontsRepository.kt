package io.github.mrvanish97.kbot.impl

import io.github.mrvanish97.kbot.impl.FontsRepository.Companion.DEFAULT_FONT_SIZE
import org.springframework.stereotype.Repository
import java.awt.Font
import java.awt.font.TextAttribute

private val fontAttributes = mapOf(
  Pair(TextAttribute.KERNING, TextAttribute.KERNING_ON)
)

private val arial = Font("Arial", Font.PLAIN, DEFAULT_FONT_SIZE).deriveFont(fontAttributes)
private val tnr = Font("Times New Roman", Font.PLAIN, DEFAULT_FONT_SIZE).deriveFont(fontAttributes)
private val cyrillicList = List(6) { arial }.plus(tnr)
private val latinList = List(3) { tnr }.plus(arial).plus(List(9) { tnr })

@Repository
class FontsRepository {

  companion object {
    const val DEFAULT_FONT_SIZE = 24
  }

  val cyrillicFonts: List<Font>
    get() = cyrillicList

  val latinFonts: List<Font>
    get() = latinList

}