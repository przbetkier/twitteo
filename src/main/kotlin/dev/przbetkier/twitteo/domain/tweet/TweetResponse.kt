package dev.przbetkier.twitteo.domain.tweet

import org.neo4j.driver.Record
import org.neo4j.driver.Value
import java.time.ZonedDateTime

data class TweetPageResponse(
    val tweets: List<TweetResponse>,
    val total: Number
) {
    companion object {
        fun fromRecord(record: Record): TweetPageResponse {

            val result = record.get("result")
            return TweetPageResponse(
                result.get("tweets").asList { TweetResponse.fromValue(it) },
                result.get("total").asLong()
            )
        }
    }
}

data class TweetResponse(
    val id: Long,
    val content: String,
    val createdAt: ZonedDateTime,
    val userId: String,
    val userName: String,
    val attachments: Set<Long>,
    val avatarUrl: String?,
    val edited: Boolean
) {
    companion object {
        fun fromValue(value: Value): TweetResponse {
            return TweetResponse(
                value.get("id").asLong(),
                value.get("content").asString(),
                value.get("createdAt").asZonedDateTime(),
                value.get("userId").asString(),
                value.get("userName").asString(),
                value.get("attachments").asList { p -> p.asLong() }.toSet(),
                value.get("avatarUrl").asString(""),
                value.get("edited").asBoolean(false)
            )
        }

        fun fromRecord(record: Record): TweetResponse {
            return TweetResponse(
                record.get("tweet").get("id").asLong(),
                record.get("tweet").get("content").asString(),
                record.get("tweet").get("createdAt").asZonedDateTime(),
                record.get("tweet").get("userId").asString(),
                record.get("tweet").get("userName").asString(),
                record.get("tweet").get("attachments").asList { p -> p.asLong() }.toSet(),
                record.get("tweet").get("avatarUrl").asString(""),
                record.get("tweet").get("edited").asBoolean(false),
            )
        }
    }
}

data class LikedTweetResponse(
    val id: Long,
    val likes: Long
) {
    companion object {
        fun fromRecord(record: Record): LikedTweetResponse {
            return LikedTweetResponse(
                record.get("tweet").get("id").asLong(),
                record.get("tweet").get("likes").asLong()
            )
        }
    }
}
