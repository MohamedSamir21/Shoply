package com.shopping.shoply.profile.presentation

/**
 * What the Profile tab shows. [initials] is null when there is no name (the avatar shows a
 * person icon). [phone] is null when none was added.
 */
data class ProfileState(
    val name: String = "",
    val initials: String? = null,
    val email: String = "",
    val phone: String? = null,
    val wishlistCount: Int = 0,
    val unreadNotificationCount: Int = 0,
    val showLogoutDialog: Boolean = false,
)
