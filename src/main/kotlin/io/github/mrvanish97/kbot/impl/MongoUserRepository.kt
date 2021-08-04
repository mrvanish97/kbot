package io.github.mrvanish97.kbot.impl

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface MongoUserRepository : MongoRepository<User, Long> {

  @get:Query("{role:'OWNER'}")
  val owner: User?

}