package dev.przbetkier.twitteo.domain.user

import com.fasterxml.jackson.annotation.JsonProperty
import mu.KotlinLogging
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserEndpoint(
    private val userService: UserService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: String): UserResponse =
        userService.getUser(userId)

    @GetMapping("/{userId}/followers")
    fun getFollowers(@PathVariable userId: String): FollowerResponse {
        return userService.getFollowers(userId)
    }

    @PostMapping("/bio")
    fun setBio(@RequestBody request: BioUpdateRequest) {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name

        return userService.updateBio(uid, request.bio)
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

data class BioUpdateRequest(
    @JsonProperty("bio") val bio: String
)
