package io.github.mrvanish97.kbot.controller

import io.github.mrvanish97.kbot.controller.node.BotNodeContext
import io.github.mrvanish97.kbot.security.TelegramUserProvider
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.util.concurrent.ConcurrentHashMap

interface UserStateRepo<T> {
  operator fun get(userId: Long): T
}

interface MutableUserState<T> : UserStateRepo<T> {
  operator fun set(userId: Long, value: T)
}

class TemporaryMutableUserState<T>(
  private val immutable: UserStateRepo<T>
) : MutableUserState<T> {
  override fun get(userId: Long): T {
    return immutable[userId]
  }

  private val setMap = hashMapOf<Long, T>()

  override fun set(userId: Long, value: T) {
    setMap[userId] = value
  }

  val events
    get() = setMap.toList()

}

typealias UserAuthPredicate = (auth: Authentication, requestedUserId: Long) -> Boolean

class SecuredMutableUserStateWrapper<T>(
  private val repo: MutableUserState<T>,
  private val getUserIsAllowed: UserAuthPredicate,
  private val setUserIsAllowed: UserAuthPredicate
) : MutableUserState<T> {

  private fun getAuth(): Authentication {
    return SecurityContextHolder.getContext().authentication
      ?: throw AuthenticationCredentialsNotFoundException(
        "To access the user's state, SecurityContextHolder should contain authentication"
      )
  }

  override fun get(userId: Long): T {
    val auth = getAuth()
    userId.toString()
    return if (getUserIsAllowed(auth, userId)) {
      repo[userId]
    } else {
      throw AccessDeniedException("Using given auth $auth, you cannot get state for userId=$userId")
    }
  }

  override fun set(userId: Long, value: T) {
    val auth = getAuth()
    return if (setUserIsAllowed(auth, userId)) {
      repo[userId] = value
    } else {
      throw AccessDeniedException("Using given auth $auth, you cannot set state for userId=$userId")
    }
  }

}

class ImmutableUserStateWrapper<T>(repo: UserStateRepo<T>) : UserStateRepo<T> by repo

class UserStateService<T : Any>(
  repo: MutableUserState<T>,
  getUserIsAllowed: UserAuthPredicate,
  setUserIsAllowed: UserAuthPredicate
) : StateService<UserStateRepo<T>, MutableUserState<T>, T> {

  private val immutableWrapper = ImmutableUserStateWrapper(repo)

  override val current = immutableWrapper

  override val updater = SecuredMutableUserStateWrapper(repo, getUserIsAllowed, setUserIsAllowed)

  override fun test(context: BotNodeContext, value: T): Boolean {
    val principle = context.auth.principal as? TelegramUserProvider ?: return false
    val userId = principle.getUser().id
    return current[userId] == value
  }

}

typealias DefaultUserStateService = UserStateService<String>

private fun defaultUserAuthPredicate(auth: Authentication, requestedUserId: Long): Boolean {
  val userId = requestedUserId.toString()
  return auth.name == userId && auth.authorities.any { it.authority == "ROLE_USER" } ||
    auth.name != userId && auth.authorities.any { it.authority == "ROLE_ADMIN" }
}

@Suppress("FunctionName")
fun DefaultUserStateService() = UserStateService(
  SimpleMapMutableUserState(ConcurrentHashMap()) {
    ""
  },
  ::defaultUserAuthPredicate,
  ::defaultUserAuthPredicate
)