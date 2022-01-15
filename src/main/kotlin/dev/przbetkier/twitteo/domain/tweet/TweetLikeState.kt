package dev.przbetkier.twitteo.domain.tweet

import org.neo4j.driver.Record

enum class TweetLikeState {
    CAN_LIKE, CAN_UNLIKE;
}

data class TweetLikeStateResponse(
    val state: TweetLikeState,
    val likes: Long
) {
    companion object {
        fun fromRecord(record: Record) =
            record.get("result").let {
                TweetLikeStateResponse(
                    TweetLikeState.valueOf(it.get("state").asString()),
                    it.get("likes").asLong()
                )
            }
    }
}
