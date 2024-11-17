package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.model.ValidationResult
import javax.inject.Inject

class ValidateConfirmedPasswordUseCase @Inject constructor() {
    operator fun invoke(password: String, confirmedPassword: String): ValidationResult {
        if (password == confirmedPassword)
            return ValidationResult(data = confirmedPassword)
        return ValidationResult(error = "The passwords don't match")
    }
}