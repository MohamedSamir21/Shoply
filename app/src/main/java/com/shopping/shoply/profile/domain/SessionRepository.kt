package com.shopping.shoply.profile.domain

/** The signed-in session. */
fun interface SessionRepository {

    /** Signs the user out on this device. Works offline. */
    fun signOut()
}
