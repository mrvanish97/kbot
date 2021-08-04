package io.github.mrvanish97.kbot.impl

interface User {
  val id: Long
  val role: Role
}

enum class Role {
  VISITOR,
  USER,
  ADMIN,
  OWNER
}