package com.shopping.shoply.authentication.domain.usecase.auth

import com.shopping.shoply.authentication.domain.interfaces.repoistory.AuthRepository
import com.shopping.shoply.authentication.domain.model.DomainContext
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class SendOTPCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        domainContext: DomainContext,
        countryCode: String,
        phoneNumber: String,
        onSent: () -> Unit,
        onAutoVerification: (Result<Unit, DataError>) -> Unit
    ) {
        authRepository.sendOTPCode(
            domainContext,
            countryCode,
            phoneNumber,
            onSent,
            onAutoVerification
        )
    }
}