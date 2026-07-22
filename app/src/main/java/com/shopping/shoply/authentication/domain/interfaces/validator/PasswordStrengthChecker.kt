package com.shopping.shoply.authentication.domain.interfaces.validator

import com.shopping.shoply.authentication.domain.model.PasswordStrength

// Port for password-strength estimation. The implementation — and the zxcvbn library it
// wraps — lives in the data layer, mirroring EmailValidator / RegexEmailValidator. Keeping
// it a SAM (fun interface) makes it trivial to fake in tests with a lambda.
// suspend: measuring is CPU work, so the data layer owns confining it off the main thread.
fun interface PasswordStrengthChecker {
    suspend fun strengthOf(password: String): PasswordStrength
}