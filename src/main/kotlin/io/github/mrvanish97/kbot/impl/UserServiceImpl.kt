//package io.github.mrvanish97.kbot.impl
//
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.data.repository.findByIdOrNull
//import org.springframework.stereotype.Service
//
//@Service
//@ConditionalOnProperty(
//  prefix = "io.uonagent.neon.bot-properties",
//  name = ["use-default-user-service-impl"],
//  havingValue = "true",
//  matchIfMissing = true
//)
//class UserServiceImpl @Autowired constructor(
//  private val mongoUserRepository: MongoUserRepository,
//  private val botProperties: BundledBotProperties
//) : UserService {
//
//  override fun getUserById(id: Long): User {
//    return mongoUserRepository.findByIdOrNull(id) ?: createDefaultUser(id)
//  }
//
//  private fun createDefaultUser(id: Long) = MongoUserImpl(id, Role.VISITOR)
//
//  override val owner: User
//    get() = mongoUserRepository.owner ?: createInitialOwner()
//
//  private fun createInitialOwner(): User {
//    val newOwner = MongoUserImpl(id = botProperties.initialOwnerId, role = Role.OWNER)
//    return mongoUserRepository.insert(newOwner)
//  }
//
//  override fun changeOwnership(newOwnerId: Long, roleForPreviousOwner: Role): User {
//    if (roleForPreviousOwner == Role.OWNER) {
//      throw IllegalArgumentException("You cannot set two active owners in the same time")
//    }
//    val previous = mongoUserRepository.owner
//    if (previous != null) {
//      saveUser(previous.id, roleForPreviousOwner)
//    }
//    return saveUser(newOwnerId, Role.OWNER)
//  }
//
//  override fun changeRole(id: Long, role: Role): User {
//    if (role == Role.OWNER) {
//      throw IllegalArgumentException("You cannot set ownership using changeRole method. use changeOwnership instead")
//    }
//    return saveUser(id, role)
//  }
//
//  private fun saveUser(id: Long, role: Role): User {
//    return if (role == Role.VISITOR) {
//      mongoUserRepository.deleteById(id)
//      createDefaultUser(id)
//    } else {
//      mongoUserRepository.save(MongoUserImpl(id, role))
//    }
//  }
//}