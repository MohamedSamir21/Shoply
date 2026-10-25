package com.shopping.shoply.cart.di

import com.shopping.shoply.cart.data.FirestoreCartRepository
import com.shopping.shoply.cart.data.FirestorePromoCodeRepository
import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.cart.domain.PromoCodeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CartDataModule {

    @Binds @Singleton
    abstract fun bindCartRepository(impl: FirestoreCartRepository): CartRepository

    @Binds @Singleton
    abstract fun bindPromoCodeRepository(impl: FirestorePromoCodeRepository): PromoCodeRepository
}
