package dev.przbetkier.twitteo.domain.attachments

import org.springframework.data.neo4j.repository.Neo4jRepository

interface AttachmentRepository: Neo4jRepository<Attachment, Long>
