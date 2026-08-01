package com.shopping.shoply.authentication.domain.usecase.validate

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

typealias UsernameValidationSuccess = Result.Success<Unit, ValidationError>
typealias UsernameValidationError = Result.Error<Unit, ValidationError>

@DisplayName("ValidateUsernameUseCase")
class ValidateUsernameUseCaseTest {
    private val validate = ValidateUsernameUseCase()

    @ParameterizedTest(name = "rejects {0}")
    @ValueSource(
        strings = [
            " ",
            ""
        ]
    )
    @DisplayName("returns EMPTY_USERNAME if the username is blank")
    fun `returns EMPTY_USERNAME if the username is blank`(
        username: String
    ) = runTest {
        assertThat(
            validate(username)
        ).isEqualTo(UsernameValidationError(ValidationError.EMPTY_USERNAME))
    }

    @Test
    @DisplayName("returns Success if the username isn't blank")
    fun `returns Success if the username isn't blank`() = runTest {
        assertThat(
            validate(username = "u")
        ).isEqualTo(UsernameValidationSuccess(Unit))
    }

}