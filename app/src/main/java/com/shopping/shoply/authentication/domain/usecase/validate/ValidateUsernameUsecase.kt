package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.model.ValidationResult
import javax.inject.Inject

class ValidateUsernameUseCase @Inject constructor() {
    operator fun invoke(username: String): ValidationResult {
        return if (username.isBlank()) {
            ValidationResult(
                error = "Your username cannot be empty"
            )
        } else {
            ValidationResult(data = username)
        }
    }
}