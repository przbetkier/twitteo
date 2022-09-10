package dev.przbetkier.twitteo.domain.user

import dev.przbetkier.twitteo.utils.PlainListConverter
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContainAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UserMentionExtractorTest {

    @ParameterizedTest
    @CsvSource(
        "Great workout with @bodytrainer today! Check his profile! | [bodytrainer]",
        "Great workout with @bodytrainer123 today! Check his profile! | [bodytrainer123]",
        "Great workout with @body_trainer today! Check his profile! | [body_trainer]",
        "Great workout with @bodytrainer today! Tomorrow gonna train with @greattrainer | [bodytrainer, greattrainer]",
        "Great workout with @bodytrainer today! Tomorrow gonna train with @greatTrainer | [bodytrainer, greatTrainer]",
        delimiter = '|'
    )
    fun `should extract mentions from text`(
        text: String,
        @PlainListConverter mentions: List<String>
    ) {
        // expect
        UserMentionExtractor.extract(text) shouldContainAll mentions
    }

    @ParameterizedTest
    @CsvSource(
        "Great w@rkout tod@y!",
        "Great w@rkout today @",
        "@ Great w@rkout today!",
    )
    fun `should not find any mentions in text`(
        text: String
    ) {
        // expect
        UserMentionExtractor.extract(text) shouldBeEqualTo emptyList()
    }
}
