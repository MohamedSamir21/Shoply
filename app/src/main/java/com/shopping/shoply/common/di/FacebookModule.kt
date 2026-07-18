package com.shopping.shoply.common.di

import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Facebook SDK objects as singletons.
 *
 * The login button's `ActivityResultContract` and [com.shopping.shoply.authentication.data.datasource
 * .FacebookAuthDataSource]'s `registerCallback` must operate on the **same** [CallbackManager], or the
 * login result never reaches the registered callback. Sharing one instance through the graph is what
 * guarantees that — so the SDK objects no longer have to be passed down from the UI through the domain.
 */
@Module
@InstallIn(SingletonComponent::class)
object FacebookModule {

    @Provides
    @Singleton
    fun provideLoginManager(): LoginManager = LoginManager.getInstance()

    @Provides
    @Singleton
    fun provideCallbackManager(): CallbackManager = CallbackManager.Factory.create()
}

/**
 * Exposes the shared [CallbackManager] to Compose, which cannot take constructor injection.
 *
 * Used by the Facebook login button to build its `ActivityResultContract` against the same instance the
 * data source registers its callback on.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface FacebookEntryPoint {
    fun callbackManager(): CallbackManager
}
