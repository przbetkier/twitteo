package dev.przbetkier.twitteo.domain.attachments

import dev.przbetkier.twitteo.utils.unwrap
import io.minio.*
import io.minio.messages.DeleteError
import io.minio.messages.DeleteObject
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class AttachmentService(
    @Qualifier("twitteoMinioClient") private val minioClient: MinioClient,
    private val attachmentRepository: AttachmentRepository
) {

    fun upload(
        file: MultipartFile,
        userId: String
    ): ObjectUploadedResponse {
        try {
            return storeInBucket(file)
                .let {
                    Attachment(
                        name = it.`object`(),
                        uploadedBy = userId
                    )
                }
                .let { attachmentRepository.save(it) }
                .let {
                    ObjectUploadedResponse(
                        id = it.id,
                        name = it.name
                    )
                }
        } catch (exception: Exception) {
            throw RuntimeException() // FIXME - to be replaced with domain exception
        }
    }

    fun getByFileId(fileId: Long): ByteArray {

        val fileName = attachmentRepository.findById(fileId).unwrap()?.name
            ?: throw RuntimeException() // FIXME - to be replaced with domain exception

        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket("twitteo")
                .`object`(fileName)
                .build()
        ).use {
            it.readAllBytes()
        }
    }

    fun getAttachments(attachmentIds: Set<Long>): Set<Attachment> =
        attachmentRepository.findAllById(attachmentIds).toSet()

    fun removeFromBucket(fileNames: List<String>) {
        logger.info { "Attachments to remove from minio: $fileNames" }
        fileNames.let {
            val results = minioClient.removeObjects(
            RemoveObjectsArgs.builder()
                .bucket("twitteo")
                .objects(it.map { name -> DeleteObject(name) })
                .build())

            for (result in results) {
                val error: DeleteError = result.get()
                logger.error (
                    "Error in deleting object " + error.objectName() + "; " + error.message()
                )
            }
        }
    }

    private fun storeInBucket(file: MultipartFile): ObjectWriteResponse {
        return minioClient.putObject(
            PutObjectArgs.builder().contentType(file.contentType)
                .bucket("twitteo")
                .`object`("${UUID.randomUUID()}_${file.originalFilename}")
                .stream(file.inputStream, file.size, -1)
                .build()
        )
    }
}
