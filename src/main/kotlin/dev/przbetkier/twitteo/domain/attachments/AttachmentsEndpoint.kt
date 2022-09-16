package dev.przbetkier.twitteo.domain.attachments

import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/attachments")
class AttachmentsEndpoint(
    private val attachmentService: AttachmentService
) {

    private val logger = KotlinLogging.logger {}

    @GetMapping(
        "/{fileId}",
        produces = [MediaType.IMAGE_JPEG_VALUE]
    )
    fun getImage(@PathVariable fileId: Long): ByteArray =
        attachmentService.getByFileId(fileId)

    @PostMapping
    fun getUploadFile(
        @RequestPart(value = "file", required = true) file: MultipartFile
    ): ObjectUploadedResponse {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val userId: String = authentication.name

        logger.info { "User [$userId] uploading a file." }

        return attachmentService.upload(file, userId)
    }
}

data class ObjectUploadedResponse(
    val id: Long?,
    val name: String
)
