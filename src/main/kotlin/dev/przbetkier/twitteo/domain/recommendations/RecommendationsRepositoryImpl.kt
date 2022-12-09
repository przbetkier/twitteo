package dev.przbetkier.twitteo.domain.recommendations

import dev.przbetkier.twitteo.domain.trending.TrendingKey
import dev.przbetkier.twitteo.domain.trending.TrendingMetadata
import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import dev.przbetkier.twitteo.domain.user.UserResponse
import org.neo4j.driver.Record
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Component

@Component
class RecommendationsRepositoryImpl(
    private val neo4jClient: Neo4jClient
) : RecommendationsRepository {
    override fun getUsersToFollow(userId: String): List<RecommendedUserResponse> {
        return neo4jClient.query {
            """
                MATCH (u: User)-[:FOLLOWS]->(common: User)-[:FOLLOWS]->(f:User)
                WHERE u.userId = ${"$"}userId  
                AND NOT (u)-[:FOLLOWS]->(f) AND u <> f
                WITH f, COLLECT(distinct common.displayName) as followedBy
                MATCH (f)<-[fol: FOLLOWS]-(:User)
                WITH f, followedBy, count(fol) as followers
                ORDER BY followers DESC, SIZE(followedBy) DESC
                LIMIT 10
                OPTIONAL MATCH (follower: User)-[:FOLLOWS]->(f)
                OPTIONAL MATCH (f)-[:FOLLOWS]->(follows:User)
                RETURN 
                {
                    userId: f.userId,
                    displayName: f.displayName,
                    bio: f.bio,
                    follows: COUNT(distinct follows),
                    followers: COUNT(distinct follower),
                    avatarUrl: f.avatarUrl
                } as user, 
                {
                    followedBy: followedBy
                } as metadata
            """.trimIndent()
        }
            .bindAll(mapOf("userId" to userId))
            .fetchAs(RecommendedUserResponse::class.java)
            .mappedBy { _, record -> RecommendedUserResponse.fromRecord(record) }
            .all().toList()
    }

    override fun getTweetsLikedByUserFollowees(userId: String): List<TweetResponse> {
        return neo4jClient.query {
            """
                MATCH (u:User)-[fr:FOLLOWS]->(fol:User)-[lr:LIKES]->(t:Tweet)<-[pr:POSTS]-(op: User)
                WHERE u.userId = ${"$"}userId
                AND t.createdAt > datetime() - duration('P7D')
                AND NOT (u)-[:FOLLOWS]->(op) AND u <> op AND NOT (u)-[:LIKES]->(t)
                WITH DISTINCT t ORDER BY t.createdAt DESC LIMIT 10
                MATCH (u:User)-[:POSTS]->(t) 
                OPTIONAL MATCH (t)-[:ATTACHES]->(a: Attachment)
                RETURN 
                $TWEET_PROJECTION
            """.trimIndent()
        }
            .bindAll(mapOf("userId" to userId))
            .fetchAs(TweetResponse::class.java)
            .mappedBy { _, record -> TweetResponse.fromRecord(record) }
            .all().toList()
    }

    override fun getSimilarTweets(userId: String): List<TweetResponse> {
        TODO("Not yet implemented")
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
    }

}

data class RecommendedUserResponse(
    val user: UserResponse,
    val metadata: RecommendationMetadata
) {
    companion object {
        fun fromRecord(record: Record) =
            RecommendedUserResponse(
                UserResponse.fromRecord(record),
                RecommendationMetadata.fromRecord(record)
            )
    }
}

data class RecommendationMetadata(
    val followedBy: List<String>
) {
    companion object {
        fun fromRecord(record: Record) =
            RecommendationMetadata(
                record.get("metadata").get("followedBy").asList { value -> value.asString() }
            )
    }
}
