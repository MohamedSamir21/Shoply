package com.shopping.shoply.home.di

import com.shopping.shoply.home.data.FirestoreHomeRepository
import com.shopping.shoply.home.domain.repository.HomeRepository
import com.shopping.shoply.notifications.data.FirestoreNotificationsRepository
import com.shopping.shoply.notifications.domain.NotificationsRepository
import com.shopping.shoply.wishlist.data.FirestoreWishlistRepository
import com.shopping.shoply.wishlist.domain.WishlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Binds each repository interface to its Firestore implementation. Lives in home/ for now because
// Home is the only consumer; it splits into per-feature modules when those features get screens.
@Module
@InstallIn(SingletonComponent::class)
abstract class HomeDataModule {

    @Binds @Singleton
    abstract fun bindHomeRepository(impl: FirestoreHomeRepository): HomeRepository

    @Binds @Singleton
    abstract fun bindWishlistRepository(impl: FirestoreWishlistRepository): WishlistRepository

    @Binds @Singleton
    abstract fun bindNotificationsRepository(impl: FirestoreNotificationsRepository): NotificationsRepository
}
