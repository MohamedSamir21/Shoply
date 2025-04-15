package com.shopping.shoply.authentication.domain.usecase.auth

import com.shopping.shoply.authentication.domain.interfaces.repoistory.AuthRepository
import com.shopping.shoply.authentication.domain.model.DomainContext
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

class SignUpWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(domainContext: DomainContext): Result<Unit, DataError> {
        return authRepository.signUpWithGoogle(domainContext)
    }
}