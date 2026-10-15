package com.shopping.shoply.authentication.domain.model

import com.shopping.shoply.common.domain.Error

// Typed validation failures: the domain reports WHAT is wrong, while presentation
// owns the (localizable) wording. Keeps user-facing English out of the domain layer.
// Implements the shared Error marker so it can ride in the same Result<D, E> the rest
// of the domain uses — one error vocabulary instead of a bespoke ValidationResult.
enum class ValidationError : Error {
    EMPTY_USERNAME,
    INVALID_EMAIL,
    INVALID_PHONE_NUMBER,
    PASSWORD_TOO_SHORT,
    PASSWORD_MISSING_LETTER_OR_DIGIT,
    PASSWORDS_DO_NOT_MATCH
}
