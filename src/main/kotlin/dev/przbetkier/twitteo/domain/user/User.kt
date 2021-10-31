package dev.przbetkier.twitteo.domain.user

import dev.przbetkier.twitteo.domain.tweet.Tweet
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING

@Node
data class User(
    @Id @GeneratedValue var id: Long? = null,
    val userId: String,
    val displayName: String,
    @Relationship(type = "POSTS", direction = OUTGOING)
    val tweets: MutableList<Tweet> = mutableListOf(),
    @Relationship(type = "FOLLOWS", direction = OUTGOING)
    val follows: MutableList<User> = mutableListOf(),
)
