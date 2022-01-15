package dev.przbetkier.twitteo.domain.user

import org.neo4j.driver.Record
import org.neo4j.driver.Value
import org.springframework.data.neo4j.core.Neo4jClient

class UserRepositoryCustomImpl(private val neo4jClient: Neo4jClient) : UserRepositoryCustom {

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

    override fun getFollowers(userId: String): FollowerResponse {
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
                    followees: COLLECT(distinct follows),
                    followers: COLLECT(distinct follower)
                } as followerResponse
            """.trimIndent()
        }
            .bindAll(parameters)
            .fetchAs(FollowerResponse::class.java)
            .mappedBy { _, record -> FollowerResponse.fromRecord(record) }
            .one().get()
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

data class FollowerResponse(
    val followers: List<Follower>,
    val followees: List<Follower>
) {
    companion object {
        fun fromRecord(record: Record): FollowerResponse {
            val node = record.get("followerResponse")
            return FollowerResponse(
                followers = node.get("followers").asList { Follower.fromValue(it) },
                followees = node.get("followees").asList { Follower.fromValue(it) },
            )
        }
    }
}

data class Follower(
    val userId: String,
    val displayName: String
) {

    companion object {
        fun fromValue(value: Value): Follower {
            return Follower(
                value.get("userId").asString(),
                value.get("displayName").asString()
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
