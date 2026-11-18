package com.shopping.shoply.editprofile.presentation

import com.shopping.shoply.authentication.presentation.utility.UiText

/**
 * [fullName] and [dateOfBirth] are the saved values; [newFullName] and [newDateOfBirth] hold
 * the user's edits until Save, so only changed fields are written.
 */
@Suppress("ArrayInDataClass") // customPhoto comes straight from Account; compared by reference.
data class EditProfileState(
    val photoUrl: String? = null,
    val customPhoto: ByteArray? = null,
    val pickedPhotoUri: String? = null,
    val fullName: String? = null,
    val newFullName: String? = null,
    val nameError: UiText? = null,
    val dateOfBirth: String? = null,
    val newDateOfBirth: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false
)

sealed interface EditProfileIntent {
    data class ChangeAvatar(val uri: String) : EditProfileIntent
    data class ChangeFullName(val newName: String) : EditProfileIntent
    data class ChangeDateOfBirth(val dateOfBirth: String) : EditProfileIntent
    data object SaveUserInfo : EditProfileIntent
}

sealed interface EditProfileEvent {
    data object ProfileUpdated : EditProfileEvent
    data class ProfileUpdateError(val error: UiText): EditProfileEvent
}
