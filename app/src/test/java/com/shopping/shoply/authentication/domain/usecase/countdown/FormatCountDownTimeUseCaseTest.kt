package com.shopping.shoply.authentication.domain.usecase.countdown

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("FormatCountDownTimeUseCase")
class FormatCountDownTimeUseCaseTest {
    private val format = FormatCountDownTimeUseCase()

    @ParameterizedTest(name = "outputs {0} -> {1}")
    @CsvSource(
        "0, 00:00",
        "59000, 00:59",
        "60000, 01:00",
        "61000, 01:01"
    )
    @DisplayName("formats time in millis into minutes and seconds format")
    fun `formats time in millis into minutes and seconds format`(
        timeInMillis: Long,
        minutesSecondsFormat: String
    ) = runTest {
        assertThat(format(timeInMillis)).isEqualTo(minutesSecondsFormat)
    }
}