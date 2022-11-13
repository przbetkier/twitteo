package dev.przbetkier.twitteo.domain.tweet

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

interface TweetRepository : Neo4jRepository<Tweet, Long>, TweetRepositoryCustom {

    @Query(
        """
            MATCH (u:User {userId: ${"$"}userId })-[]->(t:Tweet)
            WHERE ID(t) = ${"$"}tweetId
            OPTIONAL MATCH (t)-[:ATTACHES]->(a: Attachment)
            WITH t, a, collect(a.name) as attachments
            DETACH DELETE t, a
            WITH attachments
            UNWIND attachments as attachment
            RETURN attachment
        """
    )
    fun deleteTweetNew(userId: String, tweetId: Long): List<String>

    @Query(
        """
            MATCH (t: Tweet)
            WHERE ID(t) = ${"$"}tweetId
            MATCH (ref: Tweet)
            WHERE ID(ref) = ${"$"}referenceTweetId
            MERGE (t)-[:REPLIES_TO]->(ref)
            SET t:Reply
        """
    )
    fun markAsReplyTweet(tweetId: Long, referenceTweetId: Long)
}
