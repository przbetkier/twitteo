package dev.przbetkier.twitteo.domain.hashtag

import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import dev.przbetkier.twitteo.domain.tweet.TweetService
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/tags")
@RestController
class HashtagEndpoint(
    private val tweetService: TweetService,
    private val hashtagService: HashtagService
) {

    @GetMapping("/{tagId}/tweets")
    fun getTagTweets(@PathVariable tagId: String, pageable: Pageable): List<TweetResponse> {
        return tweetService.findByHashtag(tagId, pageable)
    }

    @GetMapping("/new")
    fun get10NewestHashtags() = hashtagService.findTenNewest()

}
