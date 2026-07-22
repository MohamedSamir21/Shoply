package com.shopping.shoply.authentication.data.validator

import com.google.common.truth.Truth.assertThat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

/**
 * Tests for [LibphonenumberPhoneValidator].
 *
 * [AcceptedPhones] validates a genuine example number for every region libphonenumber supports, so the
 * accepted-input coverage is broad without relying on hand-picked literals. [RejectedPhones] covers
 * malformed and unparseable input, which must be reported as invalid rather than throwing.
 *
 * The validator delegates to the real (pure JVM) libphonenumber, so these run as sociable host tests
 * with no mocking and no Android runtime.
 */
@DisplayName("LibphonenumberPhoneValidator")
class LibphonenumberPhoneValidatorTest {

    private val util = PhoneNumberUtil.getInstance()
    private val sut = LibphonenumberPhoneValidator(util)

    @Nested
    @DisplayName("isValid (accepted)")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class AcceptedPhones {

        @ParameterizedTest(name = "accepts {0}")
        @MethodSource("generatePhoneNumberPerCountry")
        fun `accepts valid phone numbers`(phoneNumber: String) {
            assertThat(sut.isValid(phoneNumber)).isTrue()
        }

        // A known-valid example number per supported region, in E.164. `mapNotNull` skips any region
        // without an example so a missing one can't fail the whole run with an NPE.
        private fun generatePhoneNumberPerCountry(): List<String> =
            util.supportedRegions.mapNotNull { region ->
                util.getExampleNumber(region)?.let { number ->
                    util.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
                }
            }
    }

    @Nested
    @DisplayName("isValid (rejected)")
    inner class RejectedPhones {

        @ParameterizedTest(name = "rejects '{0}'")
        @ValueSource(
            strings = [
                "",                 // empty
                " ",                // whitespace only
                "+",                // country code / number missing
                "abc",              // letters
                "12345",            // no leading '+' / country code
                "+2012",            // parses but too short to be valid
                "+2001207\$91614",  // stray symbol
                "+2001207a91614",   // stray letter
            ]
        )
        fun `rejects invalid phone numbers`(phoneNumber: String) {
            assertThat(sut.isValid(phoneNumber)).isFalse()
        }
    }
}
