package dev.przbetkier.twitteo.domain.tweet

import dev.przbetkier.twitteo.domain.hashtag.Hashtag
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING
import java.time.ZonedDateTime

@Node
data class Tweet(
    @Id @GeneratedValue var id: Long? = null,
    val content: String,
    val createdAt: ZonedDateTime,

    @Relationship(type = "TAGS", direction = OUTGOING)
    val hashtags: MutableList<Hashtag> = mutableListOf(),

    @Relationship(type = "REPLY_TO", direction = OUTGOING)
    val repliesTo: MutableList<Tweet> = mutableListOf(),
    @Relationship(type = "RETWEETS", direction = OUTGOING)
    val retweets: MutableList<Tweet> = mutableListOf()
)
