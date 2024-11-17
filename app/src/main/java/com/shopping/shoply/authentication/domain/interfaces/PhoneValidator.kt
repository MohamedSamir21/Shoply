package com.shopping.shoply.authentication.domain.interfaces

interface PhoneValidator {
    fun isValid(phoneNumber: String): Boolean
}