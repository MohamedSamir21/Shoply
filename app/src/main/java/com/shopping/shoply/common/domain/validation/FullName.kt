package com.shopping.shoply.common.domain.validation

import com.shopping.shoply.common.domain.Error
import com.shopping.shoply.common.domain.Result

const val MAX_FULL_NAME_LENGTH = 50

enum class FullNameError : Error { BLANK, TOO_LONG }

/** A person's full name, trimmed, or why it can't be used. Shared by Sign Up and Edit Profile (spec FR-020). */
fun validateFullName(input: String): Result<String, FullNameError> {
    val name = input.trim()
    return when {
        name.isEmpty() -> Result.Error(FullNameError.BLANK)
        name.length > MAX_FULL_NAME_LENGTH -> Result.Error(FullNameError.TOO_LONG)
        else -> Result.Success(name)
    }
}
