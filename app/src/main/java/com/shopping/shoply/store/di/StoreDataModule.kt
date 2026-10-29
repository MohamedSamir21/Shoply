package com.shopping.shoply.store.di

import com.shopping.shoply.store.data.FirestoreFavoriteStoresRepository
import com.shopping.shoply.store.domain.FavoriteStoresRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StoreDataModule {

    @Binds @Singleton
    abstract fun bindFavoriteStoresRepository(impl: FirestoreFavoriteStoresRepository): FavoriteStoresRepository
}
