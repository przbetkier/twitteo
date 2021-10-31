package dev.przbetkier.twitteo.domain.hashtag

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Repository

@Repository
interface HashtagRepository : Neo4jRepository<Hashtag, Long> {

    @Query("MATCH (h:Hashtag) RETURN h ORDER BY h.createdAt DESC LIMIT 10")
    fun findFirst10OrderByCreatedAtDesc(): List<Hashtag>

}
