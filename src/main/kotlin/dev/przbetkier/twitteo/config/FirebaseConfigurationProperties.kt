package dev.przbetkier.twitteo.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Profile

@ConfigurationProperties(prefix = "firebase")
@ConstructorBinding
@Profile("!integration")
class FirebaseConfigurationProperties(
    val projectId: String,
    val privateKeyId: String,
    val privateKey: String,
    val clientEmail: String,
    val clientId: String,
    val clientX509CertUrl: String
)
