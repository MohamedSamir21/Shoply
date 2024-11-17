package com.shopping.shoply.authentication.presentation.utility

import android.util.Patterns
import com.shopping.shoply.authentication.domain.interfaces.PhoneValidator
import java.util.regex.Pattern
import javax.inject.Inject

class PhoneValidatorUtil @Inject constructor() : PhoneValidator {
    override fun isValid(phoneNumber: String): Boolean {
        return Pattern.matches(Patterns.PHONE.pattern(), phoneNumber)
    }
}