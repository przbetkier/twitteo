package dev.przbetkier.twitteo.domain.search

import dev.przbetkier.twitteo.domain.tweet.TweetRepository
import dev.przbetkier.twitteo.domain.tweet.TweetResponse
import dev.przbetkier.twitteo.domain.user.UserRepository
import dev.przbetkier.twitteo.domain.user.UserResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/search")
class SearchEndpoint(
    private val userRepository: UserRepository,
    private val tweetRepository: TweetRepository
) {

    @GetMapping
    fun search(@RequestParam query: String): SearchResult {
        val users = userRepository.searchUserWithDisplayName(query)
        val tweets = tweetRepository.searchByContent(query)
        return SearchResult(
            users, tweets
        )
    }
}

data class SearchResult(
    val users: List<UserResponse>,
    val tweets: List<TweetResponse>
)
