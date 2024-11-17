package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.interfaces.PhoneValidator
import com.shopping.shoply.authentication.domain.model.ValidationResult
import javax.inject.Inject

class ValidatePhoneUseCase @Inject constructor(
    private val phoneValidator: PhoneValidator
) {
    operator fun invoke(phoneNumber: String): ValidationResult {
        return if (phoneValidator.isValid(phoneNumber)) {
            ValidationResult(data = phoneNumber)
        } else {
            ValidationResult(error = "Enter a valid phone number")
        }
    }
}