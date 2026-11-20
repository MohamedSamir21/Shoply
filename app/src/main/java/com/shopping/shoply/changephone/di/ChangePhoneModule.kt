package com.shopping.shoply.changephone.di

import com.shopping.shoply.changephone.data.FirebasePhoneChangeRepository
import com.shopping.shoply.changephone.domain.PhoneChangeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ChangePhoneModule {
    @Binds
    abstract fun bindPhoneChangeRepository(impl: FirebasePhoneChangeRepository): PhoneChangeRepository
}
