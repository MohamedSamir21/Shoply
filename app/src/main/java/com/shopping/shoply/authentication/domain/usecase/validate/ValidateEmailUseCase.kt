package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.interfaces.EmailValidator
import com.shopping.shoply.authentication.domain.model.ValidationResult
import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor(
    private val emailValidator: EmailValidator
) {
    operator fun invoke(email: String): ValidationResult {
        return if (emailValidator.isValid(email)) {
            ValidationResult(data = email)
        } else {
            ValidationResult(error = "Enter a valid email")
        }
    }
}