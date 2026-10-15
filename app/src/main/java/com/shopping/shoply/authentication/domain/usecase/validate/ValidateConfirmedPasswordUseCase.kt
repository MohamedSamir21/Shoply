package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class ValidateConfirmedPasswordUseCase @Inject constructor() {
    operator fun invoke(password: String, confirmedPassword: String): Result<Unit, ValidationError> {
        if (password == confirmedPassword)
            return Result.Success(Unit)
        return Result.Error(ValidationError.PASSWORDS_DO_NOT_MATCH)
    }
}