package dev.przbetkier.twitteo.domain.user

object UserMentionExtractor {

    private val mentionPattern: Regex = Regex("\\B@\\S+\\b")

    fun extract(text: String): List<String> {
        val matches = mentionPattern.findAll(text)
        return matches.map { it.groupValues.first() }.toList().map { it.filter { letter -> letter != '@' } }
    }
}
