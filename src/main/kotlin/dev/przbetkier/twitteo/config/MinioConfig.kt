package dev.przbetkier.twitteo.config

import io.minio.MinioClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig(
    private val minioProperties: MinioProperties
) {

    @Bean(name = ["twitteoMinioClient"])
    fun twitteoMinioClient(): MinioClient =
        MinioClient.builder()
            .endpoint(minioProperties.endpoint)
            .credentials(minioProperties.accessKey, minioProperties.secretKey)
            .build()
}

@ConfigurationProperties(prefix = "minio")
@ConstructorBinding
data class MinioProperties(
    val endpoint: String,
    val accessKey: String,
    val secretKey: String,
)
