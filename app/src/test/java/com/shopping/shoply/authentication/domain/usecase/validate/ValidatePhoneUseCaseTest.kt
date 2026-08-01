package com.shopping.shoply.authentication.domain.usecase.validate

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.interfaces.validator.PhoneValidator
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

typealias PhoneValidationSuccess = Result.Success<Unit, ValidationError>
typealias PhoneValidationError = Result.Error<Unit, ValidationError>

@DisplayName("ValidatePhoneUseCase")
class ValidatePhoneUseCaseTest {

    @Test
    @DisplayName("returns Success when the phone validator approves")
    fun `returns Success when the phone validator approves`() = runTest {
        val validate = ValidatePhoneUseCase { true }

        assertThat(
            validate("whatever")
        ).isEqualTo(PhoneValidationSuccess(Unit))
    }

    @Test
    @DisplayName("returns INVALID_PHONE_NUMBER when the phone validator rejects")
    fun `returns INVALID_PHONE_NUMBER when the phone validator rejects`() = runTest {
        val validate = ValidatePhoneUseCase { false }

        assertThat(
            validate("whatever")
        ).isEqualTo(PhoneValidationError(ValidationError.INVALID_PHONE_NUMBER))
    }
}