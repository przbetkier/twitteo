package dev.przbetkier.twitteo.domain.tweet

import org.springframework.data.domain.Pageable
import org.springframework.data.neo4j.core.Neo4jClient

open class TweetRepositoryCustomImpl(private val neo4jClient: Neo4jClient) : TweetRepositoryCustom {

    override fun createTweet(
        userId: String,
        hashTags: Set<String>,
        mentions: Set<String>,
        content: String
    ): TweetResponse {
        val parameters = mapOf(
            "userId" to userId,
            "hashtags" to hashTags.toList(),
            "mentions" to mentions.toList(),
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

        val mentionsClause = mentions.takeIf { it.isNotEmpty() }?.let {
            """
            UNWIND mentions AS mention
            MATCH (mentioned: User {displayName: mention})
            MERGE (tweet)-[:MENTIONS]->(mentioned)
            """.trimIndent()
        } ?: ""

        return neo4jClient.query {
            """
                MATCH (u:User {userId: ${"$"}userId })
                CREATE (t:Tweet {content: ${"$"}content, createdAt: datetime() })
                MERGE (u)-[:POSTS]->(t)
                WITH ${"$"}hashtags as hashtags, t, u, ${"$"}mentions as mentions
                $hashtagsClause
                WITH t as tweet, u, hashtags, mentions
                $mentionsClause
                WITH tweet, u, hashtags
                RETURN 
                {
                    id: id(tweet),
                    content: tweet.content,
                    createdAt: tweet.createdAt,
                    hashtags: hashtags,
                    userId: u.userId,
                    userName: u.displayName,
                    replies: 0
                } as tweet
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(TweetResponse::class.java)
            .mappedBy { _, record -> TweetResponse.fromRecord(record) }
            .all().first()
    }

    override fun deleteTweet(userId: String, tweetId: Long) {
        val parameters = mapOf(
            "userId" to userId,
            "tweetId" to tweetId,
        )

        neo4jClient.query {
            """
                MATCH (u:User {userId: ${"$"}userId })-[]->(t:Tweet)
                WHERE ID(t) = ${"$"}tweetId
                DETACH DELETE t
            """.trimIndent()
        }.bindAll(parameters).run()
    }

    override fun replyToTweet(
        userId: String,
        referenceTweetId: Long,
        hashTags: Set<String>,
        content: String
    ): TweetResponse {
        val parameters = mapOf(
            "userId" to userId,
            "referenceTweetId" to referenceTweetId,
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
                CREATE (t:Tweet:Reply {content: ${"$"}content, createdAt: datetime() })
                WITH u, t
                MATCH (reference: Tweet) WHERE id(reference) = ${"$"}referenceTweetId
                MERGE (u)-[:POSTS]->(t)
                MERGE (t)-[:REPLIES_TO]->(reference)
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
                    userName: u.displayName,
                    replies: 0
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
                WHERE NOT t:Reply // for now exclude replies
                OPTIONAL MATCH (t)-[:TAGS]->(h:Hashtag)
                OPTIONAL MATCH (r)-[:REPLIES_TO]->(t)
                RETURN 
                {
                    id: id(t),
                    content: t.content,
                    createdAt: t.createdAt,
                    hashtags: collect(DISTINCT h.name),
                    userId: u.userId,
                    userName: u.displayName,
                    replies: count(r)
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
                OPTIONAL MATCH (r)-[:REPLIES_TO]->(t)
                RETURN 
                {
                    id: id(t),
                    content: t.content,
                    createdAt: t.createdAt,
                    hashtags: collect(DISTINCT h.name),
                    userId: u.userId,
                    userName: u.displayName,
                    replies: COUNT(r)
                } as tweet
                SKIP ${"$"}offset LIMIT ${"$"}limit
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(TweetResponse::class.java)
            .mappedBy { _, record -> TweetResponse.fromRecord(record) }
            .all().toList()
    }

    override fun getReplies(referenceTweetId: Long, pageable: Pageable): List<TweetResponse> {
        val parameters = mapOf(
            "referenceTweetId" to referenceTweetId,
            "limit" to pageable.pageSize,
            "offset" to pageable.offset
        )

        return neo4jClient.query {
            """
                MATCH (u:User)-[:POSTS]->(r:Reply)-[:REPLIES_TO]->(t:Tweet)
                WHERE ID(t) = ${"$"}referenceTweetId  
                OPTIONAL MATCH (r)-[:TAGS]->(h:Hashtag)
                OPTIONAL MATCH (rtr)-[:REPLIES_TO]->(r)
                RETURN 
                {
                    id: id(r),
                    content: r.content,
                    createdAt: r.createdAt,
                    hashtags: collect(DISTINCT h.name),
                    userId: u.userId,
                    userName: u.displayName,
                    replies: COUNT(rtr)
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
                MATCH (u:User)
                WHERE u.userId = ${"$"}userId
                OPTIONAL MATCH (u)-[:POSTS]->(ut:Tweet) 
                WHERE NOT ut:Reply
                with COLLECT(distinct ut) as ut, u
                OPTIONAL MATCH (u)-[:FOLLOWS]->(f)-[:POSTS]->(ft:Tweet)
                WHERE NOT ft:Reply
                WITH COLLECT(distinct ft) + ut as tweets
                UNWIND tweets as t
                MATCH (u)-[:POSTS]->(t)
                OPTIONAL MATCH (t)-[:TAGS]->(h:Hashtag)
                OPTIONAL MATCH (r)-[:REPLIES_TO]->(t)
                WITH 
                {
                    id: id(t),
                    content: t.content,
                    createdAt: t.createdAt,
                    hashtags: collect(DISTINCT h.name),
                    userId: u.userId,
                    userName: u.displayName,
                    replies: COUNT(r)
                } as tweet
                ORDER by tweet.createdAt DESC
                SKIP ${"$"}offset LIMIT ${"$"}limit
                WITH COLLECT(tweet) as tweets

                MATCH (u:User) WHERE u.userId = ${"$"}userId
                OPTIONAL MATCH (u)-[:POSTS]->(ut:Tweet) 
                WHERE NOT ut:Reply      
                OPTIONAL MATCH (u)-[:FOLLOWS]->(f)-[:POSTS]->(ft:Tweet)
                WHERE NOT ft:Reply
                
                RETURN {
                    tweets: tweets,
                    total: COUNT(distinct ft) + COUNT(distinct ut)
                } as result
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(TweetPageResponse::class.java)
            .mappedBy { _, record -> TweetPageResponse.fromRecord(record) }
            .one().orElse(TweetPageResponse(emptyList(), 0))
    }

    override fun getLikeState(userId: String, tweetId: Long): TweetLikeStateResponse {
        val parameters = mapOf(
            "userId" to userId,
            "tweetId" to tweetId,
        )

        return neo4jClient.query {
            """
            MATCH (t: Tweet) WHERE ID(t) = ${"$"}tweetId
            OPTIONAL MATCH (u:User {userId: ${"$"}userId} )-[userLike:LIKES]->(t)
            OPTIONAL MATCH (:User)-[total:LIKES]->(t)
            WITH CASE WHEN COUNT(distinct u) = 1 THEN 'CAN_UNLIKE' else 'CAN_LIKE' END as state, COUNT(total) as likes
            RETURN 
            {
                state: state,
                likes: likes
            } as result
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(TweetLikeStateResponse::class.java)
            .mappedBy { _, record -> TweetLikeStateResponse.fromRecord(record) }
            .all().first()
    }

    override fun likeTweet(userId: String, tweetId: Long): LikedTweetResponse {
        val parameters = mapOf(
            "userId" to userId,
            "tweetId" to tweetId
        )

        return neo4jClient.query {
            """
                MATCH (t:Tweet)
                WHERE ID(t) = ${"$"}tweetId
                MATCH (u:User {userId: ${"$"}userId})
                with t as tweet, u as user
                MERGE (user)-[:LIKES]->(tweet)
                WITH tweet
                MATCH (users:User)-[:LIKES]->(tweet)
                RETURN 
                {
                    id: id(tweet),
                    likes: COUNT(users)
                } as tweet
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(LikedTweetResponse::class.java)
            .mappedBy { _, record -> LikedTweetResponse.fromRecord(record) }
            .one().orElse(LikedTweetResponse(tweetId, 0))
    }

    override fun unlikeTweet(userId: String, tweetId: Long): LikedTweetResponse {
        val parameters = mapOf(
            "userId" to userId,
            "tweetId" to tweetId
        )

        return neo4jClient.query {
            """
                MATCH (u:User {userId: ${"$"}userId})-[r:LIKES]->(t:Tweet)
                WHERE ID(t) = ${"$"}tweetId
                DELETE r
                WITH t
                MATCH (users:User)-[:LIKES]->(t)
                RETURN 
                {
                    id: id(t),
                    likes: COUNT(users)
                } as tweet
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(LikedTweetResponse::class.java)
            .mappedBy { _, record -> LikedTweetResponse.fromRecord(record) }
            .one().orElse(LikedTweetResponse(tweetId, 0))
    }

    override fun searchByContent(query: String, limit: Long): List<TweetResponse> {
        val parameters = mapOf(
            "query" to ".*(?i)$query.*",
        )

        return neo4jClient.query {
            """
                MATCH (t:Tweet)
                WHERE t.content =~ ${"$"}query
                AND NOT t:Reply // for now exclude replies
                WITH t LIMIT $limit
                MATCH (u)-[:POSTS]->(t:Tweet) 
                OPTIONAL MATCH (t)-[:TAGS]->(h:Hashtag)
                OPTIONAL MATCH (r)-[:REPLIES_TO]->(t)
                RETURN 
                {
                    id: id(t),
                    content: t.content,
                    createdAt: t.createdAt,
                    hashtags: collect(DISTINCT h.name),
                    userId: u.userId,
                    userName: u.displayName,
                    replies: count(r)
                } as tweet
                ORDER by tweet.createdAt DESC
                
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(TweetResponse::class.java)
            .mappedBy { _, record -> TweetResponse.fromRecord(record) }
            .all().toList()
    }
}
