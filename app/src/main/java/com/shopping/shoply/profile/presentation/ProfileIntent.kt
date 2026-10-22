package com.shopping.shoply.profile.presentation

sealed interface ProfileIntent {
    /** "Log Out" row: ask to confirm. */
    data object LogOutClick : ProfileIntent
    data object LogOutConfirm : ProfileIntent
    data object LogOutDismiss : ProfileIntent
}
