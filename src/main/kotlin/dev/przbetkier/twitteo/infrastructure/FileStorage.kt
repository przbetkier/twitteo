package dev.przbetkier.twitteo.infrastructure

import io.minio.*
import io.minio.messages.DeleteError
import io.minio.messages.DeleteObject
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

private val logger = KotlinLogging.logger {}

@Component
class FileStorage(
    @Qualifier("twitteoMinioClient") private val minioClient: MinioClient,
    @Value("\${minio.endpoint}") private val minioEndpoint: String
) {

    fun getFileByFilename(filename: String, bucket: String = DEFAULT_BUCKET_NAME): ByteArray =
        minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .`object`(filename)
                .build()
        ).use {
            it.readAllBytes()
        }

    fun store(file: MultipartFile, bucket: String = DEFAULT_BUCKET_NAME): ObjectWriteResponse {
        return minioClient.putObject(
            PutObjectArgs.builder().contentType(file.contentType)
                .bucket(bucket)
                .`object`("${UUID.randomUUID()}_${file.originalFilename}")
                .stream(file.inputStream, file.size, -1)
                .build()
        )
    }

    fun delete(fileNames: List<String>, bucket: String = DEFAULT_BUCKET_NAME) {
        logger.info { "Attachments to remove from minio: $fileNames" }
        fileNames.let {
            val results = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                    .bucket(bucket)
                    .objects(it.map { name -> DeleteObject(name) })
                    .build()
            )

            for (result in results) {
                val error: DeleteError = result.get()
                logger.error(
                    "Error in deleting object " + error.objectName() + "; " + error.message()
                )
            }
        }
    }

    fun getObjectUrl(filename: String, bucket: String) =
        "$minioEndpoint/$bucket/$filename"

    companion object {
        private const val DEFAULT_BUCKET_NAME = "twitteo"

    }
}
