package dev.przbetkier.twitteo.domain.tweet

import org.springframework.data.neo4j.repository.Neo4jRepository

interface TweetRepository : Neo4jRepository<Tweet, Long>, TweetRepositoryCustom
