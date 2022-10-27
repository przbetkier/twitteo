package dev.przbetkier.twitteo.domain.user

import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query

interface UserRepository : Neo4jRepository<User, Long>, UserRepositoryCustom {
    fun findByUserId(userId: String): User?

    fun findAllByDisplayNameIn(displayNames: Set<String>): List<User>

    @Query(
        """
        MATCH (u:User {userId: ${"$"}userId})
        MATCH (follower: User)-[:FOLLOWS]->(u)
        RETURN DISTINCT follower
        SKIP ${"$"}offset LIMIT ${"$"}limit   
        """
    )
    fun getFollowers(userId: String, limit: Long, offset: Long): List<User>

    @Query(
        """
        MATCH (u:User {userId: ${"$"}userId})
        MATCH (u)-[:FOLLOWS]->(follows:User)
        RETURN DISTINCT follows
        SKIP ${"$"}offset LIMIT ${"$"}limit
        """
    )
    fun getFollowees(userId: String, limit: Long, offset: Long): List<User>

    @Query(
        """
        MATCH (u:User {userId: ${"$"}userId})
        SET u.avatarUrl = ${"$"}avatarUrl
        """
    )
    fun setAvatarUrl(userId: String, avatarUrl: String)
}
