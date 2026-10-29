package com.shopping.shoply.supportchat.di

import com.shopping.shoply.supportchat.data.FirestoreSupportChatRepository
import com.shopping.shoply.supportchat.domain.SupportChatRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SupportChatDataModule {

    @Binds @Singleton
    abstract fun bindSupportChatRepository(impl: FirestoreSupportChatRepository): SupportChatRepository
}
