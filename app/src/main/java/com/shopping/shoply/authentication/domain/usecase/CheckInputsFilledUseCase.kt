package com.shopping.shoply.authentication.domain.usecase

import com.shopping.shoply.authentication.domain.model.User
import javax.inject.Inject

class CheckInputsFilledUseCase @Inject constructor() {
    operator fun invoke(user: User): Boolean {
        return user.let {
            it.username.isNotBlank() && it.email.isNotBlank() && it.phoneNumber.isNotBlank()
                    && it.password.isNotBlank() && it.confirmedPassword.isNotBlank()
        }
    }
}