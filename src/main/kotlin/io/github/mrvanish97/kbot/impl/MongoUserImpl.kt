package io.github.mrvanish97.kbot.impl

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class MongoUserImpl(
  @Id override val id: Long,
  @Indexed override val role: Role
) : User