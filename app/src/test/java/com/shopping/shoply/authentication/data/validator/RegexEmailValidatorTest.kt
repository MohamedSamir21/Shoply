package com.shopping.shoply.authentication.data.validator

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Tests for [RegexEmailValidator].
 *
 * Split by output partition: [AcceptedEmails] covers common valid address shapes, and
 * [RejectedEmails] covers malformed input (empty, missing `@`, missing top-level domain, illegal
 * characters, and similar). The validator is a pure regular-expression match with no Android
 * dependency, so these run as plain JVM tests.
 *
 * The pattern is deliberately pragmatic, so some unusual-but-accepted addresses (e.g. consecutive
 * dots in the local part) are not treated as invalid here; see [RegexEmailValidator] for the rationale.
 */
@DisplayName("RegexEmailValidator")
class RegexEmailValidatorTest {

    private val sut = RegexEmailValidator()

    @Nested
    @DisplayName("isValid (accepted)")
    inner class AcceptedEmails {

        @ParameterizedTest(name = "accepts {0}")
        @ValueSource(
            strings = [
                "user@gmail.com",
                "a@b.co",
                "user+tag@mail.co",
                "user_tag@mail.co",
                "user-tag@mail.co",
                "me@mail.co.uk",
                "first.last@x.io",
                "USER@GMAIL.COM",
                "a2a@mail.com",
                "a@m2a.com",
            ]
        )
        fun `accepts valid emails`(email: String) {
            assertThat(sut.isValid(email)).isTrue()
        }
    }

    @Nested
    @DisplayName("isValid (rejected)")
    inner class RejectedEmails {

        @ParameterizedTest(name = "rejects '{0}'")
        @ValueSource(
            strings = [
                "",         // empty input
                "a",        // no @ and no domain
                "a.b",      // no @
                "a@b\$.c",  // illegal char in domain
                "a\$@b.c",  // illegal char in local part
                "a@b.c\$",  // illegal char in TLD
                "@b.com",   // missing local part
                "a@",       // missing domain
                "a@b",      // missing dot + TLD
                "a@b.c",    // single-letter TLD (below the {2,} minimum)
                "a @b.c",   // whitespace in local part
                "a@b@c.b",  // two @ symbols
            ]
        )
        fun `rejects invalid emails`(email: String) {
            assertThat(sut.isValid(email)).isFalse()
        }
    }
}
