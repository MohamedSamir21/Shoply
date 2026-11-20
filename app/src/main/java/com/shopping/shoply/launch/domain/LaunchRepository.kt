package com.shopping.shoply.launch.domain

/** What the app needs to know at launch to pick its first screen. */
interface LaunchRepository {
    /** Signed in on this device and finished signing up (a verified email, or a Google/Facebook account). */
    fun isSignedIn(): Boolean

    suspend fun hasSeenOnboarding(): Boolean

    suspend fun markOnboardingSeen()
}
