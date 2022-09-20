package dev.przbetkier.twitteo.domain.user

import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getFollowers(userId: String, pageable: Pageable): FollowerResponse =
        userRepository.getFollowers(userId, pageable.pageSize.toLong(), pageable.offset)
            .map { it.toBasicUser() }
            .let { FollowerResponse(it) }

    fun getFollowees(userId: String, pageable: Pageable) =
        userRepository.getFollowees(userId, pageable.pageSize.toLong(), pageable.offset)
            .map { it.toBasicUser() }
            .let { FolloweeResponse(it) }

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

    fun findByUserId(userId: String) = userRepository.findByUserId(userId)
    fun getUserByDisplayName(displayName: String) =
        userRepository.getUserDataByDisplayName(displayName)

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

    fun getFollowerState(followerUid: String, followeeUid: String) =
        userRepository.getFollowerState(followerUid, followeeUid)

    fun updateBio(userId: String, bio: String) =
        userRepository.setBio(userId, bio)

    fun getMentionedUsersByContent(content: String) =
        UserMentionExtractor.extract(content).toSet().let {
            getUsersByDisplayName(it)
        }

    private fun getUsersByDisplayName(displayNames: Set<String>) =
        userRepository.findAllByDisplayNameIn(displayNames)

}
