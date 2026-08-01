package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.model.ValidationError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

// No android.util.Log here: domain stays framework-free so it can be unit-tested on the JVM.
class ValidatePasswordUseCase @Inject constructor() {
    operator fun invoke(password: String): Result<Unit, ValidationError> {
        if (password.length < 8) {
            return Result.Error(ValidationError.PASSWORD_TOO_SHORT)
        }
        val containsLetters = password.any { it.isLetter() }
        val containsDigits = password.any { it.isDigit() }
        if (!(containsLetters && containsDigits)) {
            return Result.Error(ValidationError.PASSWORD_MISSING_LETTER_OR_DIGIT)
        }
        return Result.Success(Unit)
    }
}