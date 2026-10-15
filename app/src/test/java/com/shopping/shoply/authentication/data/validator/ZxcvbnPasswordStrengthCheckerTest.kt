package com.shopping.shoply.authentication.data.validator

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.model.PasswordStrength
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Tests for [ZxcvbnPasswordStrengthChecker].
 *
 * The class has two responsibilities that are verified separately:
 *  - [ScoreToStrengthMapping] exhaustively checks the pure `scoreToStrength` mapping with fixed
 *    integer inputs, so it never depends on which password produces which zxcvbn score.
 *  - [WithTheRealLibrary] runs the actual zxcvbn library against `strengthOf` for the cases that are
 *    stable across library versions (the empty-input rule and an obviously weak password).
 */
@DisplayName("ZxcvbnPasswordStrengthChecker")
class ZxcvbnPasswordStrengthCheckerTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sut = ZxcvbnPasswordStrengthChecker(UnconfinedTestDispatcher())

    @Nested
    @DisplayName("scoreToStrength (pure mapping)")
    inner class ScoreToStrengthMapping {

        @ParameterizedTest(name = "score {0} -> {1}")
        @CsvSource(
            "0, WEAK",
            "1, WEAK",
            "2, FAIR",
            "3, GOOD",
            "4, STRONG",
            "5, STRONG", // out-of-range falls through `else`; pins the defensive behaviour
        )
        fun `maps each zxcvbn score to the matching strength`(
            score: Int,
            expected: PasswordStrength,
        ) {
            assertThat(sut.scoreToStrength(score)).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("strengthOf (with the real zxcvbn library)")
    inner class WithTheRealLibrary {

        @Test
        fun `empty password maps to NONE`() = runTest {
            val result = sut.strengthOf("")

            assertThat(result).isEqualTo(PasswordStrength.NONE)
        }

        @Test
        fun `a trivially common password maps to WEAK`() = runTest {
            // "password" is in zxcvbn's top-1000 list, so it reliably scores 0 across versions.
            val result = sut.strengthOf("password")

            assertThat(result).isEqualTo(PasswordStrength.WEAK)
        }
    }
}
