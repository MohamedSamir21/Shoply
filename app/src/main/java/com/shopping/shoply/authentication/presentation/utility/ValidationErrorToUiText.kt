package com.shopping.shoply.authentication.presentation.utility

import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.ValidationError

// Presentation owns the wording: maps domain ValidationError -> localizable UiText,
// mirroring DataError.asUiText().
fun ValidationError.asUiText(): UiText = when (this) {
    ValidationError.EMPTY_USERNAME -> UiText.StringResource(R.string.username_cannot_be_empty)
    ValidationError.INVALID_EMAIL -> UiText.StringResource(R.string.enter_valid_email)
    ValidationError.INVALID_PHONE_NUMBER -> UiText.StringResource(R.string.enter_valid_phone_number)
    ValidationError.PASSWORD_TOO_SHORT -> UiText.StringResource(R.string.password_too_short)
    ValidationError.PASSWORD_MISSING_LETTER_OR_DIGIT -> UiText.StringResource(R.string.password_letter_and_digit)
    ValidationError.PASSWORDS_DO_NOT_MATCH -> UiText.StringResource(R.string.passwords_do_not_match)
}
