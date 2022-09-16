package dev.przbetkier.twitteo.domain.hashtag

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository

@Repository
interface HashtagRepository : Neo4jRepository<Hashtag, Long> {

    @Query("MATCH (h:Hashtag) RETURN h ORDER BY h.createdAt DESC LIMIT 10")
    fun findFirst10OrderByCreatedAtDesc(): List<Hashtag>

    @Query(
        """
        WITH ${"$"}hashtags as hashtags
        UNWIND hashtags AS hashtag
        MERGE (h: Hashtag {name: hashtag})
        ON CREATE SET h.createdAt = datetime()
        RETURN h
        """)
    fun mergeHashtags(hashtags: Set<String>): Set<Hashtag>

}
