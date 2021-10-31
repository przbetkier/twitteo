package dev.przbetkier.twitteo.domain.hashtag

import dev.przbetkier.twitteo.utils.PlainListConverter
import org.amshove.kluent.shouldContainAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class HashtagExtractorTest {

    @ParameterizedTest
    @CsvSource(
        "Great #weather today! Finally #summer in #poland | [weather, summer, poland]",
        "Great #weather! Finally #summer in #poland.      | [weather, summer, poland]",
        "Great #weather, finally! #Summer in #Poland.     | [weather, summer, poland]",
        "Great #Weather, finally! #Summer in #POLAND!!    | [weather, summer, poland]",
        "Great#Weather, finally! #summer#in#poland!!      | [weather, summer, in, poland]",
        delimiter = '|'
    )
    fun `should extract hashtags from text`(
        text: String,
        @PlainListConverter hashtags: List<String>
    ) {
        // expect
        HashtagExtractor.extract(text) shouldContainAll hashtags
    }
}
