package com.shopping.shoply.authentication.domain.usecase.validate

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

typealias PasswordValidationSuccess = Result.Success<Unit, ValidationError>
typealias PasswordValidationError = Result.Error<Unit, ValidationError>

@DisplayName("ValidatePasswordUseCase")
class ValidatePasswordUseCaseTest {
    private val validate = ValidatePasswordUseCase()

    @Test
    @DisplayName("returns PASSWORD_TOO_SHORT if the password length is less than eight")
    fun `returns PASSWORD_TOO_SHORT if the password length is less than eight`() = runTest {
        val password = "passwor"

        assertThat(
            validate(password)
        ).isEqualTo(PasswordValidationError(ValidationError.PASSWORD_TOO_SHORT))
    }

    @ParameterizedTest(name = "rejects {0}")
    @ValueSource(
        strings = [
            "password",
            "01234567"
        ]
    )
    @DisplayName("returns PASSWORD_MISSING_LETTER_OR_DIGIT if the password contains only letters or only digits")
    fun `returns PASSWORD_MISSING_LETTER_OR_DIGIT if the password contains only letters or only digits`(
        password: String
    ) = runTest {
        assertThat(
            validate(password)
        ).isEqualTo(
            PasswordValidationError(ValidationError.PASSWORD_MISSING_LETTER_OR_DIGIT)
        )
    }

    @Test
    @DisplayName("returns Success if the password meets all constraints")
    fun `returns Success if the password meets all constraints`() = runTest {
        val password = "password1"

        assertThat(
            validate(password)
        ).isEqualTo(
            PasswordValidationSuccess(Unit)
        )
    }

    @Test
    @DisplayName("returns Success at the exact eight-character length boundary")
    fun `returns Success at the exact length boundary`() = runTest {
        val password = "passw0rd"

        assertThat(
            validate(password)
        ).isEqualTo(
            PasswordValidationSuccess(Unit)
        )
    }
}