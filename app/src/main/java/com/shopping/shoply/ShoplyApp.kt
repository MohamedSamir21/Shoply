package com.shopping.shoply

import android.app.Application
import com.shopping.shoply.common.platform.CurrentActivityProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShoplyApp : Application() {

    // Inject the same singleton the data layer uses, and register it so it can
    // track the current Activity for SDK calls (Google sign-in, Phone OTP).
    @Inject
    lateinit var activityProvider: CurrentActivityProvider

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(activityProvider)
    }
}