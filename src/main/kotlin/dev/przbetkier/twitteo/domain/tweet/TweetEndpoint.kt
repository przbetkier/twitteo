package dev.przbetkier.twitteo.domain.tweet

import dev.przbetkier.twitteo.domain.hashtag.HashtagExtractor
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
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
    val tweetRepository: TweetRepository
) {

    @PostMapping
    fun postTweet(@RequestBody request: TweetRequest): TweetResponse {

        val hashtags = HashtagExtractor.extract(request.content).toSet()
        val authentication: Authentication = SecurityContextHolder.getContext().authentication

        val uid: String = authentication.name

        return tweetRepository.createTweet(
            uid,
            hashtags,
            request.content
        ).also {
            logger.info {"User $uid posted tweet ${it.id} with content ${it.content} tagged with [${it.hashtags}]"}
        }
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

}

data class TweetRequest(
    val userId: String,
    val content: String,
)
