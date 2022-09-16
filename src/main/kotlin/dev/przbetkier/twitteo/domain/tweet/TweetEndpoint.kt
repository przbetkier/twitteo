package dev.przbetkier.twitteo.domain.tweet

import dev.przbetkier.twitteo.domain.hashtag.HashtagExtractor
import dev.przbetkier.twitteo.domain.user.UserMentionExtractor
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/tweets")
class TweetEndpoint(
    private val tweetRepository: TweetRepository,
    private val tweetService: TweetService
) {

    @PostMapping
    fun postTweet(
        @RequestBody request: TweetRequest
    ): TweetResponse {
        return tweetService.createTweet(request)
    }

    @GetMapping("/{userId}")
    fun getTweets(@PathVariable userId: String, pageable: Pageable): List<TweetResponse> {
        return tweetRepository.findByAuthor(userId, pageable)
    }

    @GetMapping("/feed")
    fun getTweetFeed(pageable: Pageable): TweetPageResponse {

        val authentication: Authentication = SecurityContextHolder.getContext().authentication

        val userId: String = authentication.name
        return tweetRepository.getFeed(userId, pageable)
    }

    @PostMapping("/{tweetId}/replies")
    fun reply(@RequestBody request: TweetRequest, @PathVariable tweetId: Long): TweetResponse {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val hashtags = HashtagExtractor.extract(request.content).toSet()

        val uid: String = authentication.name

        return tweetRepository.replyToTweet(
            uid,
            tweetId,
            hashtags,
            request.content
        ).also {
            logger.info { "User $uid posted tweet ${it.id} as a reply to tweet $tweetId with content ${it.content} tagged with [${hashtags.size}] tags" }
        }
    }

    @GetMapping("/{tweetId}/replies")
    fun getReplies(@PathVariable tweetId: Long, pageable: Pageable): List<TweetResponse> {
        return tweetRepository.getReplies(tweetId, pageable)
    }

    @PostMapping("/{tweetId}/like")
    fun like(@PathVariable tweetId: Long): LikedTweetResponse {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name
        return tweetRepository.likeTweet(uid, tweetId).also {
            println("Liked tweet")
            println(uid)
        }
    }

    @PostMapping("/{tweetId}/unlike")
    fun unlike(@PathVariable tweetId: Long): LikedTweetResponse {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name
        return tweetRepository.unlikeTweet(uid, tweetId).also {
            logger.info { "User [$uid] unliked tweet [$tweetId]." }
        }
    }

    @GetMapping("/{tweetId}/like-state")
    fun likeState(@PathVariable tweetId: Long): TweetLikeStateResponse {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name
        return tweetRepository.getLikeState(uid, tweetId)
    }

    @DeleteMapping("/{tweetId}")
    fun delete(@PathVariable tweetId: Long) {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name
        return tweetService.deleteTweet(tweetId, uid)
    }
}

data class TweetRequest(
    val content: String,
    val attachments: Set<Long>
)
