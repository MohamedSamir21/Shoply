package com.shopping.shoply.authentication.domain.interfaces

import com.facebook.CallbackManager
import com.shopping.shoply.authentication.domain.model.DomainContext
import com.shopping.shoply.authentication.domain.model.FacebookLoginManger
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

interface AuthRepository {
    suspend fun signUp(user: User): Result<Unit, DataError>
    suspend fun signIn(user: User): Result<Unit, DataError>
    suspend fun signUpWithGoogle(domainContext: DomainContext): Result<Unit, DataError>
    suspend fun signUpWithFacebook(
        facebookLoginManger: FacebookLoginManger,
        callbackManager: CallbackManager,
        onComplete: (Result<Unit, DataError>) -> Unit
    )
}