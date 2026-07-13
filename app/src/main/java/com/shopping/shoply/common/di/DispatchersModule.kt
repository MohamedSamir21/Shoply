package com.shopping.shoply.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

// The dispatcher is injected (not hardcoded) so the data layer owns threading explicitly
// and tests can swap in a deterministic dispatcher.
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

// UI-thread dispatcher, for work that must run on the main thread (e.g. showing a credential picker).
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}