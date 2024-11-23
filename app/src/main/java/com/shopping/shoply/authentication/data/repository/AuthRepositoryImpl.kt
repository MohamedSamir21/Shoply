package com.shopping.shoply.authentication.data.repository

import com.shopping.shoply.authentication.data.datasource.GoogleAuthClient
import com.shopping.shoply.authentication.data.datasource.IAuthDataSource
import com.shopping.shoply.authentication.domain.interfaces.AuthRepository
import com.shopping.shoply.authentication.domain.model.DomainContext
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.authentication.presentation.screen.sign_up.ActivityContext
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: IAuthDataSource,
    private val googleAuthClient: GoogleAuthClient
) : AuthRepository {

    override suspend fun signUp(user: User): Result<Unit, DataError> {
        return withContext(Dispatchers.IO) {
            authDataSource.signUp(user)
        }
    }

    override suspend fun signIn(user: User): Result<Unit, DataError> {
        return withContext(Dispatchers.IO) {
            authDataSource.signIn(user)
        }
    }

    override suspend fun signUpWithGoogle(domainContext: DomainContext): Result<Unit, DataError> {
        return withContext(Dispatchers.IO) {
            val activityContext = domainContext as ActivityContext
            googleAuthClient.signUpWithGoogle(activityContext.context)
        }
    }
}