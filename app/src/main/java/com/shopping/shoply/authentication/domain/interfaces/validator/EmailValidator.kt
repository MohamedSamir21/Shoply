package com.shopping.shoply.authentication.domain.interfaces.validator

// fun interface (SAM): a single abstract method, so tests can fake it with a lambda
// instead of a mock — e.g. EmailValidator { true }.
fun interface EmailValidator {
    fun isValid(email: String): Boolean
}