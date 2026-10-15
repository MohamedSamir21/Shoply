package com.shopping.shoply.authentication.domain.usecase.validate

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.interfaces.validator.EmailValidator
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

typealias EmailValidationSuccess = Result.Success<Unit, ValidationError>
typealias EmailValidationError = Result.Error<Unit, ValidationError>

@DisplayName("ValidateEmailUseCase")
class ValidateEmailUseCaseTest {

    @Test
    @DisplayName("returns Success when the email validator approves")
    fun `returns Success when the email validator approves`() = runTest {
        val validate = ValidateEmailUseCase(EmailValidator { true })

        assertThat(validate("whatever")).isEqualTo(EmailValidationSuccess(Unit))
    }

    @Test
    @DisplayName("returns INVALID_EMAIL when the email validator rejects")
    fun `returns INVALID_EMAIL when the email validator rejects`() = runTest {
        val validate = ValidateEmailUseCase(EmailValidator { false })

        assertThat(validate("whatever")).isEqualTo(EmailValidationError(ValidationError.INVALID_EMAIL))
    }
}
