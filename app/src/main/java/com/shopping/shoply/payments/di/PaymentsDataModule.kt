package com.shopping.shoply.payments.di

import com.shopping.shoply.payments.data.FirestoreCardWalletRepository
import com.shopping.shoply.payments.domain.CardWalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentsDataModule {

    @Binds @Singleton
    abstract fun bindCardWalletRepository(impl: FirestoreCardWalletRepository): CardWalletRepository
}
