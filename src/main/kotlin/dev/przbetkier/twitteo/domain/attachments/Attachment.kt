package dev.przbetkier.twitteo.domain.attachments

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node
data class Attachment(
    @Id @GeneratedValue var id: Long? = null,
    val name: String,
    val uploadedBy: String
)
