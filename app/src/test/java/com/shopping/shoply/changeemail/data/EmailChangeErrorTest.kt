package com.shopping.shoply.changeemail.data

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@DisplayName("EmailChangeErrorTest")
class EmailChangeErrorTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("codes")
    @DisplayName("Firebase error codes map to the messages Change Email shows")
    fun `Firebase error codes map to the messages Change Email shows`(code: String?, expected: DataError) {
        assertThat(emailChangeErrorFor(code)).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun codes() = listOf(
            Arguments.of("ERROR_WRONG_PASSWORD", DataError.Authentication.WRONG_PASSWORD),
            Arguments.of("ERROR_INVALID_CREDENTIAL", DataError.Authentication.WRONG_PASSWORD),
            Arguments.of("ERROR_EMAIL_ALREADY_IN_USE", DataError.Authentication.EMAIL_ALREADY_IN_USE),
            Arguments.of("ERROR_INVALID_EMAIL", DataError.Authentication.INVALID_EMAIL),
            Arguments.of("ERROR_REQUIRES_RECENT_LOGIN", DataError.Authentication.REQUIRES_RECENT_LOGIN),
            Arguments.of("ERROR_TOO_MANY_REQUESTS", DataError.Authentication.TOO_MANY_REQUESTS),
            Arguments.of("ERROR_SOMETHING_ELSE", DataError.Unknown),
            Arguments.of(null, DataError.Unknown),
        )
    }
}
