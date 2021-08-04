package io.github.mrvanish97.kbot.config

import io.github.mrvanish97.kbot.utils.CleanableString

private const val TOKEN_PATH_QUERY = "?token="

class QueryParamBotPathEncoder : BotPathEncoder {
  override fun encode(path: String, token: CharSequence): String {
    return "$path$TOKEN_PATH_QUERY$token"
  }

  override fun decode(fullPath: String): PathTokenPair {
    val indexOfDelimiter = fullPath.indexOf(TOKEN_PATH_QUERY)
    val path = fullPath.substring(0, indexOfDelimiter)
    val token = CleanableString(fullPath.toCharArray(startIndex = indexOfDelimiter + TOKEN_PATH_QUERY.length))
    return PathTokenPair(path, token)
  }

  override fun wipeOffToken(fullPath: String): String {
    return fullPath.substringBefore(TOKEN_PATH_QUERY)
  }

}