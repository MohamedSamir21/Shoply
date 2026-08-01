package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.interfaces.validator.EmailValidator
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor(
    private val emailValidator: EmailValidator
) {
    // Result<Unit, ValidationError>: the same Success/Error shape used across the domain.
    operator fun invoke(email: String): Result<Unit, ValidationError> {
        return if (emailValidator.isValid(email)) {
            Result.Success(Unit)
        } else {
            Result.Error(ValidationError.INVALID_EMAIL)
        }
    }
}