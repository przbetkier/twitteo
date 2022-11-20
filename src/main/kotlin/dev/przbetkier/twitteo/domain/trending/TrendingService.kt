package dev.przbetkier.twitteo.domain.trending

import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import org.springframework.stereotype.Service

@Service
class TrendingService(
    private val trendingRepository: TrendingRepository
) {

    fun getMostLikedTweets(): List<TweetResponse> =
        trendingRepository.getMostLikedTweets()

    fun getMostDiscussedTweets(): List<TweetResponse> =
        trendingRepository.getMostDiscussedTweets()

    fun getMostFollowedUsers(): List<TrendingUserResponse> =
        trendingRepository.getMostFollowedUsers()
}
