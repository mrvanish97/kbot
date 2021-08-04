package io.github.mrvanish97.kbot.impl

interface UserService {

  fun getUserById(id: Long): User

  val owner: User

  fun changeOwnership(newOwnerId: Long, roleForPreviousOwner: Role): User

  fun changeRole(id: Long, role: Role): User

}