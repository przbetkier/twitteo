package dev.przbetkier.twitteo.domain.tweet

import dev.przbetkier.twitteo.domain.hashtag.HashtagExtractor
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

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

    @GetMapping
    fun getTweets(@RequestParam userId: String, pageable: Pageable): List<TweetResponse> {
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
        return tweetService.createReply(request, tweetId)
    }

    @GetMapping("/{tweetId}/replies")
    fun getReplies(@PathVariable tweetId: Long, pageable: Pageable): TweetPageResponse {
        return tweetRepository.getReplies(tweetId, pageable)
    }

    @PostMapping("/{tweetId}/like")
    fun like(@PathVariable tweetId: Long): LikedTweetResponse {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name
        return tweetRepository.likeTweet(uid, tweetId).also {
            logger.info { "User [$uid] liked tweet [$tweetId]." }
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

    @GetMapping("/{tweetId}")
    fun getTweet(@PathVariable tweetId: Long): TweetResponse {
        return tweetService.getTweetById(tweetId)
    }

    @DeleteMapping("/{tweetId}")
    fun delete(@PathVariable tweetId: Long) {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name
        return tweetService.deleteTweet(tweetId, uid)
    }

    @PatchMapping("/{tweetId}")
    fun update(@PathVariable tweetId: Long, @RequestBody request: TweetEditRequest): TweetResponse {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name
        return tweetService.editTweet(request, uid)
    }
}

data class TweetRequest(
    val content: String,
    val attachments: Set<Long>
)

data class TweetEditRequest(
    val tweetId: Long,
    val content: String
)
