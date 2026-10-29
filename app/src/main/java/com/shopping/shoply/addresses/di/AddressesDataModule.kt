package com.shopping.shoply.addresses.di

import com.shopping.shoply.addresses.data.FirestoreAddressBookRepository
import com.shopping.shoply.addresses.domain.AddressBookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AddressesDataModule {

    @Binds @Singleton
    abstract fun bindAddressBookRepository(impl: FirestoreAddressBookRepository): AddressBookRepository
}
