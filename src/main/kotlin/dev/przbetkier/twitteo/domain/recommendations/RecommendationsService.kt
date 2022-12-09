package dev.przbetkier.twitteo.domain.recommendations

import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import org.springframework.stereotype.Service

@Service
class RecommendationsService(
    private val recommendationsRepository: RecommendationsRepository
) {
    fun getUsersToFollow(userId: String): List<RecommendedUserResponse> {
        return recommendationsRepository.getUsersToFollow(userId)
    }

    fun getTweetsLikedByUserFollowees(userId: String): List<TweetResponse> {
        return recommendationsRepository.getTweetsLikedByUserFollowees(userId)
    }

    fun getSimilarTweets(userId: String): List<TweetResponse> {
        return recommendationsRepository.getSimilarTweets(userId)
    }
}
