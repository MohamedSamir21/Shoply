package com.shopping.shoply.authentication.presentation.utility

import android.util.Patterns
import com.shopping.shoply.authentication.domain.interfaces.EmailValidator
import java.util.regex.Pattern
import javax.inject.Inject

class EmailValidatorUtil @Inject constructor() : EmailValidator {
    override fun isValid(email: String): Boolean {
        return Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(), email)
    }
}