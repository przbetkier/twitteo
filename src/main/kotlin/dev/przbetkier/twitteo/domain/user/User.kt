package dev.przbetkier.twitteo.domain.user

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node
data class User(
    @Id @GeneratedValue var id: Long? = null,
    val userId: String,
    val displayName: String,
    val bio: String? = ""
) {
    fun toBasicUser(): BasicUser =
        BasicUser(userId, displayName)
}

data class BasicUser(
    val userId: String,
    val displayName: String
)
