package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.model.SignInData
import com.shopping.shoply.authentication.domain.model.SignUpData
import javax.inject.Inject

class CheckInputsFilledUseCase @Inject constructor() {
    // Operates on primitive fields, so it depends on neither the domain entity nor a UI form.
    operator fun invoke(
        signUpData: SignUpData
    ): Boolean {
        return signUpData.run {
            username.isNotBlank() && email.isNotBlank() && phoneNumber.isNotBlank()
                    && password.isNotBlank() && confirmedPassword.isNotBlank()
        }
    }

    operator fun invoke(signInData: SignInData): Boolean =
        signInData.run { email.isNotBlank() && password.isNotBlank() }
}