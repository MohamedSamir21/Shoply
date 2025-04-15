package com.shopping.shoply.authentication.domain.interfaces.validator

interface PhoneValidator {
    fun isValid(phoneNumber: String): Boolean
}