package dev.przbetkier.twitteo.domain.trending

import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import dev.przbetkier.twitteo.domain.user.UserResponse
import org.neo4j.driver.Record
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class TrendingRepositoryImpl(
    private val neo4jClient: Neo4jClient
) : TrendingRepository {

    override fun getMostLikedTweets(): List<TweetResponse> {

        return neo4jClient.query {
            """
                MATCH (t:Tweet)<-[r:LIKES]-()
                WHERE t.createdAt > datetime() - duration('P7D')
                AND NOT t:Reply
                WITH t, count(r) as likes
                ORDER BY likes DESC
                LIMIT 10
                MATCH (u:User)-[:POSTS]->(t) 
                OPTIONAL MATCH (t)-[:ATTACHES]->(a: Attachment)
                RETURN 
                $TWEET_PROJECTION
            """.trimIndent()
        }
            .fetchAs(TweetResponse::class.java)
            .mappedBy { _, record -> TweetResponse.fromRecord(record) }
            .all().toList()
    }

    override fun getMostDiscussedTweets(): List<TweetResponse> {
        return neo4jClient.query {
            """
                MATCH p=(t:Tweet)<-[rt:REPLIES_TO*..50]-(:Tweet)
                WHERE NOT t:Reply AND t.createdAt > datetime() - duration('P7D')
                WITH t, COUNT(distinct rt) as comments
                ORDER BY comments DESC
                LIMIT 10
                MATCH (u:User)-[:POSTS]->(t) 
                OPTIONAL MATCH (t)-[:ATTACHES]->(a: Attachment)
                RETURN 
                $TWEET_PROJECTION
            """.trimIndent()
        }
            .fetchAs(TweetResponse::class.java)
            .mappedBy { _, record -> TweetResponse.fromRecord(record) }
            .all().toList()
    }

    override fun getMostFollowedUsers(): List<TrendingUserResponse> {
        return neo4jClient.query {
            """
                MATCH (u:User)<-[follow:FOLLOWS]-(:User)
                WHERE follow.since > datetime() - duration("P7D")
                WITH u, count(follow) as newFollowers
                ORDER BY newFollowers DESC
                LIMIT 10
                MATCH (u)
                OPTIONAL MATCH (follower: User)-[:FOLLOWS]->(u)
                OPTIONAL MATCH (u)-[:FOLLOWS]->(follows:User)
                RETURN
                $MOST_FOLLOWED_USER_PROJECTION
            """.trimIndent()
        }
            .fetchAs(TrendingUserResponse::class.java)
            .mappedBy { _, record -> TrendingUserResponse.fromRecord(record) }
            .all().toList()
    }

    companion object {
        const val TWEET_PROJECTION = """
            {
                id: id(t),
                content: t.content,
                createdAt: t.createdAt,
                attachments: collect(DISTINCT ID(a)),
                userId: u.userId,
                userName: u.displayName,
                avatarUrl: u.avatarUrl,
                edited: t.edited
            } as tweet
        """

        const val MOST_FOLLOWED_USER_PROJECTION = """
            {
                userId: u.userId,
                displayName: u.displayName,
                bio: u.bio,
                follows: COUNT(distinct follows),
                followers: COUNT(distinct follower),
                avatarUrl: u.avatarUrl
            } as user, {
                key: "MOST_FOLLOWED",
                score: newFollowers
            } as metadata
        """
    }
}

data class TrendingUserResponse(
    val user: UserResponse,
    val metadata: TrendingMetadata
) {
    companion object {
        fun fromRecord(record: Record) =
            TrendingUserResponse(
                UserResponse.fromRecord(record),
                TrendingMetadata.fromRecord(record)
            )
    }
}

data class TrendingMetadata(
    val key: TrendingKey,
    val score: Long
) {
    companion object {
        fun fromRecord(record: Record) =
            record.get("metadata").let {
                TrendingMetadata(
                    TrendingKey.valueOf(it.get("key").asString()),
                    it.get("score").asLong()
                )
            }
    }
}

enum class TrendingKey {
    MOST_LIKED,
    MOST_FOLLOWED,
    MOST_DISCUSSED
}
