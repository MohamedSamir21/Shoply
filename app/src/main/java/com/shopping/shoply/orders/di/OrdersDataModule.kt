package com.shopping.shoply.orders.di

import com.shopping.shoply.orders.data.FirestoreOrdersRepository
import com.shopping.shoply.orders.domain.OrdersRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OrdersDataModule {

    @Binds @Singleton
    abstract fun bindOrdersRepository(impl: FirestoreOrdersRepository): OrdersRepository
}
