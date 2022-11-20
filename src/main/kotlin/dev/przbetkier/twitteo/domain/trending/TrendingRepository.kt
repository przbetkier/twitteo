package dev.przbetkier.twitteo.domain.trending

import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import dev.przbetkier.twitteo.domain.user.UserResponse
import org.springframework.stereotype.Repository

@Repository
interface TrendingRepository {

    fun getMostLikedTweets(): List<TweetResponse>
    fun getMostDiscussedTweets(): List<TweetResponse>
    fun getMostFollowedUsers(): List<TrendingUserResponse>
}
