package dev.przbetkier.twitteo.domain.user

import dev.przbetkier.twitteo.infrastructure.FileStorage
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

private val logger = KotlinLogging.logger {}

@Service
class UserService(
    private val userRepository: UserRepository,
    private val fileStorage: FileStorage
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
                displayName = request.displayName,
                avatarUrl = null
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

    fun updateProfile(userId: String, request: ProfileUpdateRequest): UserResponse {
        request.bio?.let { userRepository.setBio(userId, it) }
        request.avatarUrl?.let { userRepository.setAvatarUrl(userId, it) }
        return getUser(userId)
    }

    fun uploadAvatar(file: MultipartFile, userId: String): AvatarUploadResponse {
        val uploadedFileName = fileStorage.store(file, "avatars").`object`()
        val avatarUrl = fileStorage.getObjectUrl(uploadedFileName, "avatars")
        return AvatarUploadResponse(avatarUrl)
    }

    fun getMentionedUsersByContent(content: String) =
        UserMentionExtractor.extract(content).toSet().let {
            getUsersByDisplayName(it)
        }

    private fun getUsersByDisplayName(displayNames: Set<String>) =
        userRepository.findAllByDisplayNameIn(displayNames)

}
