package dev.przbetkier.twitteo.domain.recommendations

import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import dev.przbetkier.twitteo.domain.user.UserResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recommendations")
class RecommendationsEndpoint(
    private val recommendationsService: RecommendationsService
) {

    @GetMapping("/users/to-follow")
    fun getUsersToFollow(): List<RecommendedUserResponse> =
        SecurityContextHolder.getContext().authentication.name
            .let { userId -> recommendationsService.getUsersToFollow(userId) }


    @GetMapping("/tweets/liked-by-followees")
    fun getTweetsLikedByUserFollowees(): List<TweetResponse> =
        SecurityContextHolder.getContext().authentication.name
            .let { userId -> recommendationsService.getTweetsLikedByUserFollowees(userId) }

    @GetMapping("/tweets/similar")
    fun getSimilarTweets(): List<TweetResponse> =
        SecurityContextHolder.getContext().authentication.name
            .let { userId -> recommendationsService.getSimilarTweets(userId) }

}
