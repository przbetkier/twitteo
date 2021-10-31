package dev.przbetkier.twitteo.domain.hashtag

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import java.time.ZonedDateTime

@Node
data class Hashtag(
    @Id @GeneratedValue var id: Long? = null,
    val name: String,
    val createdAt: ZonedDateTime
)

