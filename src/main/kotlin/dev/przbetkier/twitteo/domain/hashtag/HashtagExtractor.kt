package dev.przbetkier.twitteo.domain.hashtag

import java.util.regex.Matcher
import java.util.regex.Pattern

object HashtagExtractor {

    // FIXME (przbetkier) - That pattern would not work on hastags with polish letter on 1st position #źle or #żubr
    private val hashtag_pattern: Pattern = Pattern.compile("#(\\w+)")

    fun extract(text: String): List<String> {
        val mat: Matcher = hashtag_pattern.matcher(text)
        val hashtags: MutableList<String> = ArrayList()
        while (mat.find()) {
            hashtags.add(mat.group(1))
        }
        return hashtags.map { it.lowercase() }.toList()
    }
}
