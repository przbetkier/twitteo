package dev.przbetkier.twitteo.domain.hashtag

import dev.przbetkier.twitteo.domain.tweet.TweetRepository
import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/tags")
@RestController
class HashtagEndpoint(
    private val tweetRepository: TweetRepository,
    private val hashtagRepository: HashtagRepository
) {

    @GetMapping("/{tagId}/tweets")
    fun getTagTweets(@PathVariable tagId: String, pageable: Pageable): List<TweetResponse> {
        return tweetRepository.findByHashtag(tagId, pageable)
    }

    @GetMapping("/new")
    fun get10NewestHashtags() = hashtagRepository.findFirst10OrderByCreatedAtDesc()

}
