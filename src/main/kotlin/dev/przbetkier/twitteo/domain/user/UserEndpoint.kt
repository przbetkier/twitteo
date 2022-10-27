package dev.przbetkier.twitteo.domain.user

import com.fasterxml.jackson.annotation.JsonProperty
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
class UserEndpoint(
    private val userService: UserService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: String): UserResponse =
        userService.getUser(userId)

    @GetMapping
    fun getUserByDisplayName(@RequestParam displayName: String): UserResponse =
        userService.getUserByDisplayName(displayName)

    @GetMapping("/{userId}/followers")
    fun getFollowers(@PathVariable userId: String, pageable: Pageable): FollowerResponse {
        return userService.getFollowers(userId, pageable)
    }

    @GetMapping("/{userId}/followees")
    fun getFollowees(@PathVariable userId: String, pageable: Pageable): FolloweeResponse {
        return userService.getFollowees(userId, pageable)
    }

    @PostMapping("/profile")
    fun updateProfile(@RequestBody request: ProfileUpdateRequest): UserResponse {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name

        return userService.updateProfile(uid, request)
    }

    @PostMapping("/avatar")
    fun uploadAvatar(
        @RequestPart(value = "file", required = true) file: MultipartFile
    ): AvatarUploadResponse {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val userId: String = authentication.name

        logger.info { "User [$userId] uploading an avatar." }

        return userService.uploadAvatar(file, userId)
    }

    @PostMapping("/{followee}/followers")
    fun follow(@PathVariable followee: String): FollowerState {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name

        return userService.follow(uid, followee).also {
            logger.info { "User $uid now follows $followee" }
        }
    }

    @DeleteMapping("/{followee}/followers")
    fun unfollow(@PathVariable followee: String): FollowerState {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name
        return userService.unfollow(uid, followee).also {
            logger.info { "User $uid unfollowed $followee" }
        }
    }

    @GetMapping("/{followee}/follower-state")
    fun canFollow(@PathVariable followee: String): FollowerState {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        return if (isAnonymous(authentication) || authentication.name.equals(followee)) {
            FollowerState.CANNOT_FOLLOW
        } else {
            userService.getFollowerState(authentication.name, followee)
        }
    }


    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest) {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name

        userService.createUser(uid, request).also {
            logger.info { "Created user node: $uid" }
        }
    }

    private fun isAnonymous(authentication: Authentication): Boolean {
        return authentication.authorities.map { it.authority }.contains("ROLE_ANONYMOUS")
    }
}

data class CreateUserRequest(
    @JsonProperty("displayName") val displayName: String
)

data class ProfileUpdateRequest(
    val bio: String?,
    val avatarUrl: String?
)

data class AvatarUploadResponse(
    @JsonProperty("url") val url: String
)

data class FollowerResponse(
    val followers: List<BasicUser>,
)

data class FolloweeResponse(
    val followees: List<BasicUser>
)
