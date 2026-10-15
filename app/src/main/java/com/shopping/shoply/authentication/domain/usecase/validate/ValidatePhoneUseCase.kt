package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.interfaces.validator.PhoneValidator
import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class ValidatePhoneUseCase @Inject constructor(
    private val phoneValidator: PhoneValidator
) {
    operator fun invoke(phoneNumber: String): Result<Unit, ValidationError> {
        return if (phoneValidator.isValid(phoneNumber)) {
            Result.Success(Unit)
        } else {
            Result.Error(ValidationError.INVALID_PHONE_NUMBER)
        }
    }
}