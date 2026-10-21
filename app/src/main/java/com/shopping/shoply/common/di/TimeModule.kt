package com.shopping.shoply.common.di

import com.shopping.shoply.common.domain.time.CurrentHourProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Calendar

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {
    // Calendar, not java.time: minSdk 24 has no java.time without desugaring.
    @Provides
    fun provideCurrentHourProvider(): CurrentHourProvider =
        CurrentHourProvider { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
}
