package io.github.mrvanish97.kbot.config

typealias PathTokenPair = Pair<String, CharSequence>

interface BotPathEncoder {
  
  fun encode(path: String, token: CharSequence): String

  fun decode(fullPath: String): PathTokenPair

  fun wipeOffToken(fullPath: String): String
  
}