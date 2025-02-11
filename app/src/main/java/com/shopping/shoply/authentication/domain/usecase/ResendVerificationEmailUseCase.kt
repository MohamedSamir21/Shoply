package com.shopping.shoply.authentication.domain.usecase

import com.shopping.shoply.authentication.domain.interfaces.AuthRepository
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class ResendVerificationEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    suspend operator fun invoke(): Result<Unit, DataError> {
        return authRepository.resendVerificationEmailLink()
    }
}