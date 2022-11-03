package dev.przbetkier.twitteo.domain.attachments

import dev.przbetkier.twitteo.infrastructure.FileStorage
import dev.przbetkier.twitteo.utils.unwrap
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

private val logger = KotlinLogging.logger {}

@Service
class AttachmentService(
    private val attachmentRepository: AttachmentRepository,
    private val fileStorage: FileStorage
) {

    fun upload(
        file: MultipartFile,
        userId: String
    ): ObjectUploadedResponse {
        try {
            return fileStorage.store(file)
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

        return fileStorage.getFileByFilename(fileName)
    }

    fun getAttachments(attachmentIds: Set<Long>): Set<Attachment> =
        attachmentRepository.findAllById(attachmentIds).toSet()

    fun deleteAttachments(fileNames: List<String>) {
        fileStorage.delete(fileNames)
    }
}
