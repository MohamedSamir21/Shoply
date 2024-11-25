package com.shopping.shoply.authentication.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.authentication.data.datasource.FacebookAuthDataSource
import com.shopping.shoply.authentication.data.datasource.FirebaseAuthDataSource
import com.shopping.shoply.authentication.data.datasource.GoogleAuthDataSource
import com.shopping.shoply.authentication.data.datasource.IAuthDataSource
import com.shopping.shoply.authentication.data.repository.AuthRepositoryImpl
import com.shopping.shoply.authentication.domain.interfaces.AuthRepository
import com.shopping.shoply.authentication.domain.interfaces.EmailValidator
import com.shopping.shoply.authentication.domain.interfaces.PhoneValidator
import com.shopping.shoply.authentication.presentation.utility.EmailValidatorUtil
import com.shopping.shoply.authentication.presentation.utility.PhoneValidatorUtil
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseAuthentication(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideCredentialManger(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(
            context
        )
    }

    @Provides
    @Singleton
    fun provideGoogleAuthDataSource(
        credentialManager: CredentialManager,
        firebaseAuth: FirebaseAuth
    ): GoogleAuthDataSource {
        return GoogleAuthDataSource(
            credentialManager,
            firebaseAuth
        )
    }

    @Provides
    @Singleton
    fun provideFacebookAuthDataSource(
        firebaseAuth: FirebaseAuth
    ): FacebookAuthDataSource {
        return FacebookAuthDataSource(firebaseAuth)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingsModule {

    @Binds
    abstract fun provideAuthDataSource(firebaseAuthDataSource: FirebaseAuthDataSource): IAuthDataSource

    @Binds
    abstract fun provideAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun provideEmailValidator(emailValidatorUtil: EmailValidatorUtil): EmailValidator

    @Binds
    abstract fun providePhoneValidator(phoneValidatorUtil: PhoneValidatorUtil): PhoneValidator
}