package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class ValidateUsernameUseCase @Inject constructor() {
    operator fun invoke(username: String): Result<Unit, ValidationError> {
        return if (username.isBlank()) {
            Result.Error(ValidationError.EMPTY_USERNAME)
        } else {
            Result.Success(Unit)
        }
    }
}