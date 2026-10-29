package com.shopping.shoply.search.di

import com.shopping.shoply.search.data.DataStoreRecentSearchesRepository
import com.shopping.shoply.search.domain.RecentSearchesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchDataModule {

    @Binds @Singleton
    abstract fun bindRecentSearchesRepository(impl: DataStoreRecentSearchesRepository): RecentSearchesRepository
}
