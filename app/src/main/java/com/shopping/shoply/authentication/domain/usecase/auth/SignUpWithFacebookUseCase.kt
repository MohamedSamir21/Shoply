package com.shopping.shoply.authentication.domain.usecase.auth

import com.facebook.CallbackManager
import com.shopping.shoply.authentication.domain.interfaces.repoistory.AuthRepository
import com.shopping.shoply.authentication.domain.model.FacebookLoginManger
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class SignUpWithFacebookUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        facebookLoginManger: FacebookLoginManger,
        callbackManager: CallbackManager,
        onComplete: (Result<Unit, DataError>) -> Unit
    ) {
        authRepository.signUpWithFacebook(
            facebookLoginManger,
            callbackManager,
            onComplete
        )
    }
}