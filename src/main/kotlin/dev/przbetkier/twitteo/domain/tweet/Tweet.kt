package dev.przbetkier.twitteo.domain.tweet

import dev.przbetkier.twitteo.domain.attachments.Attachment
import dev.przbetkier.twitteo.domain.hashtag.Hashtag
import dev.przbetkier.twitteo.domain.user.User
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.core.schema.Relationship.Direction.INCOMING
import org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING
import java.time.ZonedDateTime

@Node
data class Tweet(
    @Id @GeneratedValue var id: Long? = null,
    val content: String,
    val createdAt: ZonedDateTime,

    @Relationship(type = "TAGS", direction = OUTGOING)
    val hashtags: MutableList<Hashtag> = mutableListOf(),
    @Relationship(type = "ATTACHES", direction = OUTGOING)
    val attachments: MutableList<Attachment> = mutableListOf(),
    @Relationship(type = "LIKES", direction = INCOMING)
    val usersWhoLiked: MutableList<User> = mutableListOf(),
    @Relationship(type = "MENTIONS", direction = OUTGOING)
    val mentionedUsers: MutableList<User> = mutableListOf(),
    @Relationship(type = "POSTS", direction = INCOMING)
    val userWhoPosted: User,
    val edited: Boolean = false
) {

    fun toTweetResponse(avatarUrl: String?) = TweetResponse(
        id!!,
        content,
        createdAt,
        userWhoPosted.userId,
        userWhoPosted.displayName,
        attachments.map { attachment -> attachment.id!! }.toSet(),
        avatarUrl,
        edited
    )
}
