package com.shopping.shoply.checkout.di

import com.shopping.shoply.checkout.data.FirestoreOrderRepository
import com.shopping.shoply.checkout.data.FirestorePaymentRepository
import com.shopping.shoply.checkout.domain.OrderRepository
import com.shopping.shoply.checkout.domain.PaymentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CheckoutDataModule {

    @Binds @Singleton
    abstract fun bindPaymentRepository(impl: FirestorePaymentRepository): PaymentRepository

    @Binds @Singleton
    abstract fun bindOrderRepository(impl: FirestoreOrderRepository): OrderRepository
}
