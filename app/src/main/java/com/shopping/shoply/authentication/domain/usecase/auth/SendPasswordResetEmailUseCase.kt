package com.shopping.shoply.authentication.domain.usecase.auth

import com.shopping.shoply.authentication.domain.interfaces.repository.AuthRepository
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class SendPasswordResetEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit, DataError> =
        authRepository.sendPasswordResetEmail(email)
}