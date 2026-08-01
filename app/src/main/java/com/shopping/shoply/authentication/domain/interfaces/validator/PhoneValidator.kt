package com.shopping.shoply.authentication.domain.interfaces.validator

// fun interface (SAM): a single abstract method, so tests can fake it with a lambda
// instead of a mock — e.g. PhoneValidator { true }.
fun interface PhoneValidator {
    fun isValid(phoneNumber: String): Boolean
}