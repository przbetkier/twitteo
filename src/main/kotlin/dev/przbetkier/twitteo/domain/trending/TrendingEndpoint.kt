package dev.przbetkier.twitteo.domain.trending

import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/trending")
class TrendingEndpoint(
    private val trendingService: TrendingService
) {

    @GetMapping("/tweets/most-liked")
    fun getMostLikedTweets(): List<TweetResponse> =
        trendingService.getMostLikedTweets()

    @GetMapping("/tweets/most-discussed")
    fun getMostDiscussedTweets(): List<TweetResponse> =
        trendingService.getMostDiscussedTweets()

    @GetMapping("/users/most-followed")
    fun getMostFollowed(): List<TrendingUserResponse> =
        trendingService.getMostFollowedUsers()
}
