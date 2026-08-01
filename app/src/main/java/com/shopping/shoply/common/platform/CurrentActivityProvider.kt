package com.shopping.shoply.common.platform

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

// Tracks the foreground Activity via lifecycle callbacks, held weakly to avoid leaks.
// Registered once in ShoplyApp; injected wherever the data layer needs an Activity.
@Singleton
class CurrentActivityProvider @Inject constructor() :
    ActivityProvider, Application.ActivityLifecycleCallbacks {

    private var activityRef: WeakReference<Activity>? = null

    override fun current(): Activity? = activityRef?.get()

    override fun onActivityResumed(activity: Activity) {
        activityRef = WeakReference(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (activityRef?.get() === activity) activityRef = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
}