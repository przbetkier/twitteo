package dev.przbetkier.twitteo.domain.tweet

import dev.przbetkier.twitteo.domain.attachments.AttachmentService
import dev.przbetkier.twitteo.domain.hashtag.HashtagService
import dev.przbetkier.twitteo.domain.user.UserService
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Service
class TweetService(
    private val tweetRepository: TweetRepository,
    private val attachmentService: AttachmentService,
    private val userService: UserService,
    private val hashtagService: HashtagService
) {

    @Transactional
    fun createTweet(request: TweetRequest): TweetResponse {
        val hashtags = hashtagService.getHashtagFromContent(request.content)
        val mentions = userService.getMentionedUsersByContent(request.content)
        val attachments = attachmentService.getAttachments(request.attachments)

        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val uid: String = authentication.name

        return userService.findByUserId(uid)?.let { user ->
            val tweetToSave = Tweet(
                userWhoPosted = user,
                content = request.content,
                hashtags = hashtags.toMutableList(),
                attachments = attachments.toMutableList(),
                mentionedUsers = mentions.toMutableList(),
                usersWhoLiked = mutableListOf(),
                createdAt = ZonedDateTime.now(),
            )

            tweetRepository.save(tweetToSave).let {
                logger.info { "User $uid posted tweet ${it.id} with content ${it.content} tagged with [${it.hashtags.size}] hashtags" }
                it.toTweetResponse(user.avatarUrl)
            }
        } ?: throw RuntimeException() // FIXME fix with domain exception

    }

    fun findByHashtag(tagId: String, pageable: Pageable) =
        tweetRepository.findByHashtag(tagId, pageable)

    @Transactional
    fun deleteTweet(tweetId: Long, userId: String) {
        tweetRepository.deleteTweetNew(userId, tweetId).also { attachments ->
            logger.info { "Tweet $tweetId has been deleted" }
            attachmentService.deleteAttachments(attachments)
            logger.info { "Tweet $tweetId attachments have been removed" }
        }
    }

    fun getTweetById(tweetId: Long): TweetResponse {
        return tweetRepository.findById(tweetId).map {
            it.toTweetResponse(it.userWhoPosted.avatarUrl)
        }.orElseThrow { RuntimeException() }
    }

    fun createReply(request: TweetRequest, referenceTweetId: Long): TweetResponse {
        val tweet = createTweet(request)
        tweetRepository.markAsReplyTweet(tweet.id, referenceTweetId)
        return tweet
    }

    @Transactional
    fun editTweet(request: TweetEditRequest, userId: String): TweetResponse {
        return tweetRepository.findById(request.tweetId).map {
            if (canBeEdited(it, userId)) {
                val hashtags = hashtagService.getHashtagFromContent(request.content)
                val updatedTweet = it.copy(
                    content = request.content,
                    hashtags = hashtags.toMutableList(),
                    edited = true
                )
                tweetRepository.save(updatedTweet)
                    .toTweetResponse(it.userWhoPosted.avatarUrl)
                    .also { logger.info { "User $userId updated tweet ${it.id} with content ${it.content}" } }
            } else {
                throw RuntimeException()
            }
        }.orElseThrow { RuntimeException() }
    }

    private fun canBeEdited(tweet: Tweet, userId: String): Boolean =
        ChronoUnit.MINUTES.between(tweet.createdAt, ZonedDateTime.now()) < 5 && tweet.userWhoPosted.userId == userId
}
