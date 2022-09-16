package dev.przbetkier.twitteo.domain.user

import org.springframework.data.neo4j.repository.Neo4jRepository

interface UserRepository : Neo4jRepository<User, Long>, UserRepositoryCustom {
    fun findByUserId(userId: String): User?

    fun findAllByDisplayNameIn(displayNames: Set<String>): List<User>
}
