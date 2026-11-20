package com.shopping.shoply.launch.di

import com.shopping.shoply.launch.data.DefaultLaunchRepository
import com.shopping.shoply.launch.domain.LaunchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LaunchModule {

    @Binds @Singleton
    abstract fun bindLaunchRepository(impl: DefaultLaunchRepository): LaunchRepository
}
