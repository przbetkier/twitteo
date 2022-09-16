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

        val savedTweet = userService.findByUserId(uid)?.let { user ->
            val tweetToSave = Tweet(
                userWhoPosted = user,
                content = request.content,
                hashtags = hashtags.toMutableList(),
                attachments = attachments.toMutableList(),
                mentionedUsers = mentions.toMutableList(),
                usersWhoLiked = mutableListOf(),
                createdAt = ZonedDateTime.now(),
            )
            tweetRepository.save(tweetToSave).also {
                logger.info { "User $uid posted tweet ${it.id} with content ${it.content} tagged with [${it.hashtags.size}] hashtags" }
            }
        }

        return savedTweet?.toTweetResponse() ?: throw RuntimeException() // FIXME fix with domain exception
    }

    fun findByHashtag(tagId: String, pageable: Pageable) =
        tweetRepository.findByHashtag(tagId, pageable)

    @Transactional
    fun deleteTweet(tweetId: Long, userId: String) {
        tweetRepository.deleteTweetNew(userId, tweetId).also {
            attachments ->
            logger.info { "Tweet $tweetId has been deleted" }
            attachmentService.removeFromBucket(attachments)
            logger.info { "Tweet $tweetId attachments have been removed" }
        }
    }
}
