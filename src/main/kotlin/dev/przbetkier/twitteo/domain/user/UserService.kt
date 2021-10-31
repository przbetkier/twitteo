package dev.przbetkier.twitteo.domain.user

import mu.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getFollowers(userId: String): FollowerResponse = userRepository.getFollowers(userId)

    fun createUser(userId: String, request: CreateUserRequest) {

        userRepository.findByUserId(userId)?.let {
            throw RuntimeException("User with ID: $userId already exists")
            // FIXME throw custom exception
        } ?: logger.info { "User $userId does not exist yet, trying to create new one" }

        userRepository.save(
            User(
                userId = userId,
                displayName = request.displayName
            )
        )
    }

    fun getUser(userId: String) = userRepository.getUserData(userId)

    fun follow(followerUid: String, followeeUid: String): FollowerState {
        logger.info { "User $followerUid wants to follow $followeeUid" }

        return userRepository.follow(followerUid, followeeUid).also {
            logger.info { "User $followerUid started following $followeeUid." }
        }
    }

    fun unfollow(followerUid: String, followeeUid: String): FollowerState {
        logger.info { "User $followerUid wants to unfollow $followeeUid" }

        return userRepository.unfollow(followerUid, followeeUid).also {
            logger.info { "User $followerUid stopped following $followeeUid." }
        }
    }

    fun getFollowerState(followerUid: String, followeeUid: String): FollowerState {
        return userRepository.getFollowerState(followerUid, followeeUid)
    }
}
