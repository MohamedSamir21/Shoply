package com.shopping.shoply.authentication.data.validator

import com.shopping.shoply.authentication.domain.interfaces.validator.EmailValidator
import javax.inject.Inject

/**
 * Default [EmailValidator], implementing the domain port in the data layer.
 *
 * Validation is a single, framework-free regular-expression match, so the class is pure: it has no
 * Android or platform dependencies and the same input always yields the same result.
 *
 * The pattern is intentionally pragmatic rather than fully RFC 5322 compliant — it accepts the common
 * `local@domain.tld` shape (letters, digits, and `+ _ . -` in the local part; a dotted domain; and a
 * top-level domain of at least two letters). Exhaustive RFC validation is impractical with a regex
 * and offers little real-world benefit; deliverability is confirmed separately by email verification.
 *
 * @see EmailValidator the domain port this fulfils
 */
class RegexEmailValidator @Inject constructor() : EmailValidator {

    override fun isValid(email: String): Boolean = EMAIL_REGEX.matches(email)

    private companion object {
        // `matches` requires the whole input to match; the anchors document that intent explicitly.
        val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
