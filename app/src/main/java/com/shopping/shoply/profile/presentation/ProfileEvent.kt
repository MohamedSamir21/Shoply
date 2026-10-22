package com.shopping.shoply.profile.presentation

sealed interface ProfileEvent {
    /** The user signed out; leave the signed-in app. */
    data object SignedOut : ProfileEvent
}
