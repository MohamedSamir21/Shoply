package com.shopping.shoply.authentication.domain.interfaces.validator

interface EmailValidator {
    fun isValid(email: String): Boolean
}