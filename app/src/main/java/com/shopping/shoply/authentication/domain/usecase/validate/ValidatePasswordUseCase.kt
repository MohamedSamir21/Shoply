package com.shopping.shoply.authentication.domain.usecase.validate

import android.util.Log
import com.shopping.shoply.authentication.domain.model.ValidationResult
import javax.inject.Inject

private const val TAG = "ValidatePasswordUseCase"

class ValidatePasswordUseCase @Inject constructor() {
    operator fun invoke(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(error = "Your password must be at least 8 letters")
        }
        val containsLetters = password.any { it.isLetter() }
        val containsDigits = password.any { it.isDigit() }
        Log.d(TAG, containsDigits.toString())
        if (!(containsLetters && containsDigits)){
            Log.d(TAG, "not contains digits or letters")
            return ValidationResult(error = "Your password must contain at least one letter and at least one digit")
        }
        return ValidationResult(data = password)
    }
}