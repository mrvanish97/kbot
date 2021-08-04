package io.github.mrvanish97.kbot.controller

import java.io.File
import java.io.InputStream

sealed class Media(val inputStream: InputStream, val name: String) {
  constructor(file: File): this(file.inputStream(), file.name)
}

