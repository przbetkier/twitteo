package dev.przbetkier.twitteo.domain.user

import org.neo4j.driver.Record
import org.springframework.data.neo4j.core.Neo4jClient

class UserRepositoryCustomImpl(
    private val neo4jClient: Neo4jClient
) : UserRepositoryCustom {

    // TBRemoved
    override fun getUserData(userId: String): UserResponse {
        val parameters = mapOf(
            "userId" to userId,
        )

        return neo4jClient.query {
            """
                MATCH (u:User {userId: ${"$"}userId})
                OPTIONAL MATCH (follower: User)-[:FOLLOWS]->(u)
                OPTIONAL MATCH (u)-[:FOLLOWS]->(follows:User)
                RETURN 
                {
                    userId: u.userId,
                    displayName: u.displayName,
                    bio: u.bio,
                    follows: COUNT(distinct follows),
                    followers: COUNT(distinct follower)
                } as user
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(UserResponse::class.java)
            .mappedBy { _, record -> UserResponse.fromRecord(record) }
            .all().first()
    }

    override fun getUserDataByDisplayName(displayName: String): UserResponse {
        val parameters = mapOf(
            "name" to displayName,
        )

        return neo4jClient.query {
            """
                MATCH (u:User {displayName: ${"$"}name})
                OPTIONAL MATCH (follower: User)-[:FOLLOWS]->(u)
                OPTIONAL MATCH (u)-[:FOLLOWS]->(follows:User)
                RETURN 
                {
                    userId: u.userId,
                    displayName: u.displayName,
                    bio: u.bio,
                    follows: COUNT(distinct follows),
                    followers: COUNT(distinct follower)
                } as user
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(UserResponse::class.java)
            .mappedBy { _, record -> UserResponse.fromRecord(record) }
            .all().first()
    }

    override fun getFollowerState(followerUid: String, followeeUid: String): FollowerState {
        val parameters = mapOf(
            "followerUid" to followerUid,
            "followeeUid" to followeeUid,
        )

        return neo4jClient.query {
            """
            MATCH (u:User {userId: ${"$"}followerUid} )-[:FOLLOWS]->(u2: User {userId: ${"$"}followeeUid} )
            RETURN CASE WHEN COUNT(u) = 1 THEN 'FOLLOWS' else 'DOES_NOT_FOLLOW' END as state
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(FollowerState::class.java)
            .mappedBy { _, record -> FollowerState.fromRecord(record) }
            .all().first()
    }

    override fun follow(followerUid: String, followeeUid: String): FollowerState {
        val parameters = mapOf(
            "followerUid" to followerUid,
            "followeeUid" to followeeUid,
        )

        return neo4jClient.query {
            """
            MATCH (u:User {userId: ${"$"}followerUid})
            MATCH (u2: User {userId: ${"$"}followeeUid})
            CREATE (u)-[:FOLLOWS {since: datetime()}]->(u2)
            RETURN 'FOLLOWS' as state
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(FollowerState::class.java)
            .mappedBy { _, record -> FollowerState.fromRecord(record) }
            .all().first()
    }

    override fun unfollow(followerUid: String, followeeUid: String): FollowerState {
        val parameters = mapOf(
            "followerUid" to followerUid,
            "followeeUid" to followeeUid,
        )

        return neo4jClient.query {
            """
            MATCH (u:User {userId: ${"$"}followerUid})-[f:FOLLOWS]->(u2: User {userId: ${"$"}followeeUid})
            DELETE f
            RETURN 'DOES_NOT_FOLLOW' as state
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(FollowerState::class.java)
            .mappedBy { _, record -> FollowerState.fromRecord(record) }
            .all().first()
    }

    override fun setBio(userId: String, bio: String) {
        val parameters = mapOf(
            "userId" to userId,
            "bio" to bio
        )

        neo4jClient.query {
            """
                MATCH (u:User {userId: ${"$"}userId})
                SET u.bio = ${"$"}bio
            """.trimIndent()
        }
            .bindAll(parameters)
            .run()
    }

    override fun searchUserWithDisplayName(query: String, limit: Long): List<UserResponse> {
        val parameters = mapOf(
            "query" to ".*(?i)$query.*",
        )

        return neo4jClient.query {
            """
                MATCH (u:User)
                WHERE u.displayName =~ ${"$"}query
                WITH u LIMIT $limit
                OPTIONAL MATCH (follower: User)-[:FOLLOWS]->(u)
                OPTIONAL MATCH (u)-[:FOLLOWS]->(follows:User)
                RETURN 
                {
                    userId: u.userId,
                    displayName: u.displayName,
                    bio: u.bio,
                    follows: COUNT(distinct follows),
                    followers: COUNT(distinct follower)
                } as user
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(UserResponse::class.java)
            .mappedBy { _, record -> UserResponse.fromRecord(record) }
            .all().toList()
    }
}

data class UserResponse(
    val userId: String,
    val displayName: String,
    val followers: Long,
    val follows: Long,
    val bio: String
) {
    companion object {
        fun fromRecord(record: Record) =
            record.get("user").let {
                UserResponse(
                    it.get("userId").asString(),
                    it.get("displayName").asString(),
                    it.get("followers").asLong(),
                    it.get("follows").asLong(),
                    it.get("bio").asString("")
                )
            }
    }
}

enum class FollowerState {
    FOLLOWS, DOES_NOT_FOLLOW, CANNOT_FOLLOW;

    companion object {
        fun fromRecord(record: Record) =
            valueOf(record.get("state").asString())
    }
}
