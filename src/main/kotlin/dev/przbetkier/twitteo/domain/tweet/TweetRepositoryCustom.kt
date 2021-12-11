package dev.przbetkier.twitteo.domain.tweet

import org.springframework.data.domain.Pageable

interface TweetRepositoryCustom {

    fun getFeed(userId: String, pageable: Pageable): TweetPageResponse
    fun createTweet(userId: String, hashTags: Set<String>, content: String): TweetResponse
    fun findByAuthor(userId: String, pageable: Pageable): List<TweetResponse>
    fun findByHashtag(tagId: String, pageable: Pageable): List<TweetResponse>
    fun getReplies(referenceTweetId: Long, pageable: Pageable): List<TweetResponse>
    fun replyToTweet(userId: String, referenceTweetId: Long, hashTags: Set<String>, content: String): TweetResponse
}
