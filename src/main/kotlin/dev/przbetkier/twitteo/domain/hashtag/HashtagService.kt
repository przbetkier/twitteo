package dev.przbetkier.twitteo.domain.hashtag

import org.springframework.stereotype.Service

@Service
class HashtagService(
    private val hashtagRepository: HashtagRepository
) {

    fun getHashtagFromContent(content: String) =
        HashtagExtractor.extract(content).toSet().let {
            hashtagRepository.mergeHashtags(it)
        }

    fun findTenNewest() = hashtagRepository.findFirst10OrderByCreatedAtDesc()
}
