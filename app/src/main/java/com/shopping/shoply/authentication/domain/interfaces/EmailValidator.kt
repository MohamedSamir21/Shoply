package com.shopping.shoply.authentication.domain.interfaces

interface EmailValidator {
    fun isValid(email: String): Boolean
}