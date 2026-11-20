package com.shopping.shoply.changeemail.di

import com.shopping.shoply.changeemail.data.FirebaseEmailChangeRepository
import com.shopping.shoply.changeemail.domain.EmailChangeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ChangeEmailModule {
    @Binds
    abstract fun bindEmailChangeRepository(impl: FirebaseEmailChangeRepository): EmailChangeRepository
}
