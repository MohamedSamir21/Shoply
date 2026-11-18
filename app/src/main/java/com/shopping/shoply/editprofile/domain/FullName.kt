package com.shopping.shoply.editprofile.domain

import com.shopping.shoply.common.domain.Error
import com.shopping.shoply.common.domain.Result

const val MAX_FULL_NAME_LENGTH = 50

enum class FullNameError : Error { BLANK, TOO_LONG }

/** The trimmed name, or why it can't be saved (spec FR-020). */
fun validateFullName(input: String): Result<String, FullNameError> {
    val name = input.trim()
    return when {
        name.isEmpty() -> Result.Error(FullNameError.BLANK)
        name.length > MAX_FULL_NAME_LENGTH -> Result.Error(FullNameError.TOO_LONG)
        else -> Result.Success(name)
    }
}
