package com.shopping.shoply.authentication.domain.usecase.auth

import com.shopping.shoply.authentication.domain.interfaces.repository.AuthRepository
import com.shopping.shoply.authentication.domain.model.SendOtpEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendOTPCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(
        countryCode: String,
        phoneNumber: String,
        link: Boolean = false
    ): Flow<SendOtpEvent> = authRepository.sendOTPCode(countryCode, phoneNumber, link)
}