package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.validation.FullNameError
import com.shopping.shoply.common.domain.validation.validateFullName
import javax.inject.Inject

/** Sign Up's view of the shared full-name rule, so a name valid here is also valid in Edit Profile. */
class ValidateUsernameUseCase @Inject constructor() {
    operator fun invoke(username: String): Result<Unit, ValidationError> =
        when (val result = validateFullName(username)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(
                when (result.error) {
                    FullNameError.BLANK -> ValidationError.EMPTY_USERNAME
                    FullNameError.TOO_LONG -> ValidationError.USERNAME_TOO_LONG
                }
            )
        }
}