package com.shopping.shoply.authentication.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.shopping.shoply.authentication.data.datasource.auth.FacebookAuthDataSource
import com.shopping.shoply.authentication.data.datasource.auth.FirebaseAuthDataSource
import com.shopping.shoply.authentication.data.datasource.auth.GoogleAuthDataSource
import com.shopping.shoply.authentication.data.datasource.auth.AuthDataSource
import com.shopping.shoply.authentication.data.datasource.countryinfo.LocaleDataSource
import com.shopping.shoply.authentication.data.datasource.countryinfo.TelephonyManagerDataSource
import com.shopping.shoply.authentication.data.repository.AuthRepositoryImpl
import com.shopping.shoply.authentication.data.repository.CountryInfoRepositoryImpl
import com.shopping.shoply.authentication.domain.interfaces.repository.AuthRepository
import com.shopping.shoply.authentication.domain.interfaces.repository.CountryInfoRepository
import com.shopping.shoply.authentication.domain.interfaces.validator.EmailValidator
import com.shopping.shoply.authentication.domain.interfaces.validator.PhoneValidator
import com.shopping.shoply.authentication.domain.usecase.countdown.FormatCountDownTimeUseCase
import com.shopping.shoply.authentication.data.validator.RegexEmailValidator
import com.shopping.shoply.authentication.data.validator.LibphonenumberPhoneValidator
import com.shopping.shoply.authentication.data.validator.ZxcvbnPasswordStrengthChecker
import com.shopping.shoply.authentication.domain.interfaces.validator.PasswordStrengthChecker
import com.shopping.shoply.common.di.DefaultDispatcher
import com.shopping.shoply.common.platform.ActivityProvider
import com.shopping.shoply.common.platform.CurrentActivityProvider
import dagger.Binds
import kotlinx.coroutines.CoroutineDispatcher
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

    // GoogleAuthDataSource & FacebookAuthDataSource have @Inject constructors, so Hilt
    // builds them directly — and picks up the new ActivityProvider dependency for free.

    @Provides
    @Singleton
    fun provideFormatCountDownTimeUseCase() = FormatCountDownTimeUseCase()

    @Provides
    @Singleton
    fun provideCountryInfoRepository(
        telephonyManagerDataSource: TelephonyManagerDataSource,
        localeDataSource: LocaleDataSource,
        phoneNumberUtil: PhoneNumberUtil,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
    ): CountryInfoRepository =
        CountryInfoRepositoryImpl(
            telephonyManagerDataSource,
            localeDataSource,
            phoneNumberUtil,
            defaultDispatcher
        )

    @Provides
    @Singleton
    fun provideLocaleDataSource(): LocaleDataSource = LocaleDataSource()

    @Provides
    @Singleton
    fun providePhoneNumberUtil(): PhoneNumberUtil = PhoneNumberUtil.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingsModule {

    @Binds
    @Singleton
    abstract fun provideAuthDataSource(firebaseAuthDataSource: FirebaseAuthDataSource): AuthDataSource

    @Binds
    abstract fun provideAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun provideEmailValidator(emailValidatorUtil: RegexEmailValidator): EmailValidator

    @Binds
    abstract fun providePhoneValidator(phoneValidatorUtil: LibphonenumberPhoneValidator): PhoneValidator

    // @Singleton here matches the class scope: the dictionary-loading checker is built once
    // and shared, unlike the cheap stateless validators above.
    @Binds
    @Singleton
    abstract fun providePasswordStrengthChecker(
        impl: ZxcvbnPasswordStrengthChecker
    ): PasswordStrengthChecker

    // Binds the lifecycle-tracking implementation to the neutral ActivityProvider port.
    @Binds
    @Singleton
    abstract fun provideActivityProvider(impl: CurrentActivityProvider): ActivityProvider
}