package io.github.mrvanish97.kbot.controller

fun interface UserStateStringFactory<T : Any> {

  fun fromString(value: String) : T

}

object DefaultUserStateStringFactory : UserStateStringFactory<String> {
  override fun fromString(value: String): String {
    return value
  }
}