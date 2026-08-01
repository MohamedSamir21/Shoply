package com.shopping.shoply.authentication.domain.usecase.auth

import com.shopping.shoply.authentication.domain.interfaces.repository.AuthRepository
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    // Sign-in only needs credentials, not the whole profile entity.
    suspend operator fun invoke(email: String, password: String): Result<Unit, DataError> =
        authRepository.signIn(email, password)
}