package com.shopping.shoply.profile.domain.model

/**
 * The signed-in user as the Profile tab shows them. [phone] is null when none was added.
 * [customPhoto] is the JPEG the user picked in Edit Profile; [photoUrl] is the sign-in provider's photo.
 * [emailVerified] and [phoneVerified] come from the sign-in account: the email was confirmed, and
 * the phone number is linked as a sign-in method. [pendingEmail] is a new address waiting for its
 * confirmation link; [canChangeEmail] is false for accounts that sign in only with Google or Facebook.
 */
@Suppress("ArrayInDataClass") // Compared by reference on purpose: decoded fresh from each snapshot.
data class Account(
    val name: String,
    val email: String,
    val phone: String?,
    val photoUrl: String? = null,
    val customPhoto: ByteArray? = null,
    val dateOfBirth: String? = null,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val pendingEmail: String? = null,
    val canChangeEmail: Boolean = false
)

/** The photo to show for this account: the one picked in Edit Profile, else the provider's. */
val Account.avatarPhoto: Any? get() = customPhoto ?: photoUrl

/** Changes from Edit Profile; null fields stay as they are. [pickedPhotoUri] is a local content URI. */
data class UserInfo(
    val name: String?,
    val pickedPhotoUri: String?,
    val dateOfBirth: String?
)
