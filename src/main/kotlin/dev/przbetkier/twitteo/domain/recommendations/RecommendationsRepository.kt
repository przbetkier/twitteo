package dev.przbetkier.twitteo.domain.recommendations

import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import org.springframework.stereotype.Repository

@Repository
interface RecommendationsRepository {

    fun getUsersToFollow(userId: String): List<RecommendedUserResponse>
    fun getTweetsLikedByUserFollowees(userId: String): List<TweetResponse>
    fun getSimilarTweets(userId: String): List<TweetResponse>

}
