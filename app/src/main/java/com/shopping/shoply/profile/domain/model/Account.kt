package com.shopping.shoply.profile.domain.model

/**
 * The signed-in user as the Profile tab shows them. [phone] is null when none was added.
 * [customPhoto] is the JPEG the user picked in Edit Profile; [photoUrl] is the sign-in provider's photo.
 */
@Suppress("ArrayInDataClass") // Compared by reference on purpose: decoded fresh from each snapshot.
data class Account(
    val name: String,
    val email: String,
    val phone: String?,
    val photoUrl: String? = null,
    val customPhoto: ByteArray? = null,
    val dateOfBirth: String? = null
)

/** Changes from Edit Profile; null fields stay as they are. [pickedPhotoUri] is a local content URI. */
data class UserInfo(
    val name: String?,
    val pickedPhotoUri: String?,
    val dateOfBirth: String?
)
