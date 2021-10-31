package dev.przbetkier.twitteo.domain.tweet

import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient

open class TweetRepositoryCustomImpl(private val neo4jClient: Neo4jClient) : TweetRepositoryCustom {

    override fun createTweet(userId: String, hashTags: Set<String>, content: String): TweetResponse {
        val parameters = mapOf(
            "userId" to userId,
            "hashtags" to hashTags.toList(),
            "content" to content
        )

        val hashtagsClause = hashTags.takeIf { it.isNotEmpty() }?.let {
            """
            UNWIND hashtags AS hashtag
            MERGE (h: Hashtag {name: hashtag})
            ON CREATE SET h.createdAt = datetime()
            MERGE (t)-[:TAGS]->(h)
            """.trimIndent()
        } ?: ""

        return neo4jClient.query {
            """
                MATCH (u:User {userId: ${"$"}userId })
                CREATE (t:Tweet {content: ${"$"}content, createdAt: datetime() })
                MERGE (u)-[:POSTS]->(t)
                WITH ${"$"}hashtags as hashtags, t, u
                $hashtagsClause
                WITH t as tweet, u, hashtags
                RETURN 
                {
                    id: id(tweet),
                    content: tweet.content,
                    createdAt: tweet.createdAt,
                    hashtags: hashtags,
                    userId: u.userId,
                    userName: u.displayName
                } as tweet
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(TweetResponse::class.java)
            .mappedBy { _, record -> TweetResponse.fromRecord(record) }
            .all().first()
    }

    override fun findByAuthor(userId: String, pageable: Pageable): List<TweetResponse> {
        val parameters = mapOf(
            "userId" to userId,
            "limit" to pageable.pageSize,
            "offset" to pageable.offset
        )

        return neo4jClient.query {
            """
                MATCH (u:User {userId: ${"$"}userId})-[:POSTS]->(t:Tweet)
                OPTIONAL MATCH (t)-[:TAGS]->(h:Hashtag)
                RETURN 
                {
                    id: id(t),
                    content: t.content,
                    createdAt: t.createdAt,
                    hashtags: collect(DISTINCT h.name),
                    userId: u.userId,
                    userName: u.displayName
                } as tweet
                ORDER by tweet.createdAt DESC
                SKIP ${"$"}offset LIMIT ${"$"}limit
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(TweetResponse::class.java)
            .mappedBy { _, record -> TweetResponse.fromRecord(record) }
            .all().toList()
    }

    override fun findByHashtag(tagId: String, pageable: Pageable): List<TweetResponse> {
        val parameters = mapOf(
            "hashtag" to tagId,
            "limit" to pageable.pageSize,
            "offset" to pageable.offset
        )

        return neo4jClient.query {
            """
                MATCH (u:User)-[:POSTS]->(t:Tweet)-[:TAGS]->(h:Hashtag {name: ${"$"}hashtag})
                RETURN 
                {
                    id: id(t),
                    content: t.content,
                    createdAt: t.createdAt,
                    hashtags: collect(DISTINCT h.name),
                    userId: u.userId,
                    userName: u.displayName
                } as tweet
                SKIP ${"$"}offset LIMIT ${"$"}limit
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(TweetResponse::class.java)
            .mappedBy { _, record -> TweetResponse.fromRecord(record) }
            .all().toList()
    }

    override fun getFeed(userId: String, pageable: Pageable): TweetPageResponse {
        val parameters = mapOf(
            "userId" to userId,
            "limit" to pageable.pageSize,
            "offset" to pageable.offset
        )

        return neo4jClient.query {
            """
                MATCH (u:User { userId: ${"$"}userId })-[:FOLLOWS]->(f: User)-[:POSTS]->(t: Tweet)
                OPTIONAL MATCH (t)-[:TAGS]->(h:Hashtag)
                WITH 
                {
                    id: id(t),
                    content: t.content,
                    createdAt: t.createdAt,
                    hashtags: collect(DISTINCT h.name),
                    userId: f.userId,
                    userName: f.displayName
                } as tweet
                ORDER by tweet.createdAt DESC
                SKIP ${"$"}offset LIMIT ${"$"}limit
                WITH COLLECT(tweet) as tweets
                MATCH (u:User { userId: ${"$"}userId })-[:FOLLOWS]->(f: User)-[:POSTS]->(t: Tweet)
                RETURN {
                    tweets: tweets,
                    total: COUNT(t)
                } as result
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(TweetPageResponse::class.java)
            .mappedBy { _, record -> TweetPageResponse.fromRecord(record) }
            .one().orElse(TweetPageResponse(emptyList(), 0))
    }
}
