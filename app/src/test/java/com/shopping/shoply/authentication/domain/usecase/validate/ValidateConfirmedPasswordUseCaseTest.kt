package com.shopping.shoply.authentication.domain.usecase.validate

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

typealias ConfirmationSuccess = Result.Success<Unit, ValidationError>
typealias ConfirmationError = Result.Error<Unit, ValidationError>

@DisplayName("ValidateConfirmedPasswordUseCase")
class ValidateConfirmedPasswordUseCaseTest {
    private val confirmMatching = ValidateConfirmedPasswordUseCase()
    private val password = "password"

    @Test
    @DisplayName("returns success if the two passwords match")
    fun `returns success if the two passwords match`() = runTest {
        assertThat(
            confirmMatching(password, password)
        ).isEqualTo(ConfirmationSuccess(Unit))
    }

    @Test
    @DisplayName("returns PASSWORDS_DO_NOT_MATCH if the two passwords don't match")
    fun `returns PASSWORDS_DO_NOT_MATCH if the two passwords don't match`() = runTest {
        assertThat(
            confirmMatching(password, password.replace('p', 'b'))
        ).isEqualTo(ConfirmationError(ValidationError.PASSWORDS_DO_NOT_MATCH))
    }
}