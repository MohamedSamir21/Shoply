package com.shopping.shoply.profile.di

import com.shopping.shoply.profile.data.FirebaseSessionRepository
import com.shopping.shoply.profile.data.FirestoreProfileRepository
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileDataModule {

    @Binds @Singleton
    abstract fun bindProfileRepository(impl: FirestoreProfileRepository): ProfileRepository

    @Binds @Singleton
    abstract fun bindSessionRepository(impl: FirebaseSessionRepository): SessionRepository
}
