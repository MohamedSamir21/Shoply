package com.shopping.shoply.common.di

import com.shopping.shoply.common.domain.time.CurrentHourProvider
import com.shopping.shoply.common.domain.time.NowProvider
import com.shopping.shoply.common.domain.time.StartOfDayProvider
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

    @Provides
    fun provideStartOfDayProvider(): StartOfDayProvider = StartOfDayProvider {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Provides
    fun provideNowProvider(): NowProvider = NowProvider { System.currentTimeMillis() }
}
