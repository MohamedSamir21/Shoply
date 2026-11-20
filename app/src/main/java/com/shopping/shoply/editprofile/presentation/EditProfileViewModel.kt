package com.shopping.shoply.editprofile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.changeemail.domain.EmailChangeRepository
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.validation.FullNameError
import com.shopping.shoply.common.domain.validation.MAX_FULL_NAME_LENGTH
import com.shopping.shoply.editprofile.domain.dateOfBirthFromPicker
import com.shopping.shoply.editprofile.domain.todayAsUtcMidnight
import com.shopping.shoply.common.domain.validation.validateFullName
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val emailChangeRepository: EmailChangeRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()
    private val _events = Channel<EditProfileEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Live, so a saved photo or name shows up as soon as Firestore has it.
        viewModelScope.launch {
            profileRepository.observeAccount().collect { account ->
                _state.update {
                    it.copy(
                        photoUrl = account.photoUrl,
                        customPhoto = account.customPhoto,
                        fullName = account.name,
                        email = account.email,
                        emailVerified = account.emailVerified,
                        pendingEmail = account.pendingEmail,
                        canChangeEmail = account.canChangeEmail,
                        dateOfBirth = account.dateOfBirth,
                        phoneNumber = account.phone,
                        phoneVerified = account.phoneVerified,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onIntent(intent: EditProfileIntent) = when (intent) {
        is EditProfileIntent.ChangeAvatar -> _state.update { it.copy(pickedPhotoUri = intent.uri) }
        is EditProfileIntent.ChangeDateOfBirth -> changeDateOfBirth(intent.pickedUtcMillis)
        is EditProfileIntent.ChangeFullName -> _state.update { it.copy(newFullName = intent.newName, nameError = null) }
        EditProfileIntent.SaveUserInfo -> save()
        EditProfileIntent.ChangeEmail -> changeEmail()
        EditProfileIntent.ResendEmailLink -> resendEmailLink()
        EditProfileIntent.CancelEmailChange -> cancelEmailChange()
    }

    /** FR-036: Google and Facebook own the email of accounts without a password. */
    private fun changeEmail() {
        val event = if (state.value.canChangeEmail) {
            EditProfileEvent.OpenChangeEmail()
        } else {
            EditProfileEvent.Message(UiText.StringResource(R.string.email_managed_by_provider))
        }
        _events.trySend(event)
    }

    private fun resendEmailLink() {
        val pending = state.value.pendingEmail ?: return
        viewModelScope.launch {
            when (val result = emailChangeRepository.resendEmailChangeLink(pending)) {
                is Result.Success -> _events.send(EditProfileEvent.Message(UiText.StringResource(R.string.verification_link_sent)))
                // The password check expired: Change Email asks for it again.
                is Result.Error -> if (result.error == DataError.Authentication.REQUIRES_RECENT_LOGIN) {
                    _events.send(EditProfileEvent.OpenChangeEmail(askForPassword = true))
                } else {
                    _events.send(EditProfileEvent.Message(result.error.asUiText()))
                }
            }
        }
    }

    private fun cancelEmailChange() {
        emailChangeRepository.cancelEmailChange()
        _state.update { it.copy(pendingEmail = null) }
        _events.trySend(EditProfileEvent.Message(UiText.StringResource(R.string.email_change_cancelled)))
    }

    private fun changeDateOfBirth(pickedUtcMillis: Long) {
        // The calendar already blocks future days; this guards against anything that slips through.
        val date = dateOfBirthFromPicker(pickedUtcMillis, todayAsUtcMidnight(System.currentTimeMillis()))
        if (date is Result.Success) _state.update { it.copy(newDateOfBirth = date.data) }
    }

    private fun save() {
        val current = state.value
        if (current.isLoading || current.isSaving) return

        val name = current.newFullName?.let { typed ->
            when (val validation = validateFullName(typed)) {
                is Result.Success -> validation.data.takeIf { it != current.fullName }
                is Result.Error -> {
                    _state.update { it.copy(nameError = validation.error.asUiText()) }
                    return
                }
            }
        }
        val update = UserInfo(
            name = name,
            pickedPhotoUri = current.pickedPhotoUri,
            dateOfBirth = current.newDateOfBirth?.takeIf { it != current.dateOfBirth }
        )
        if (update == UserInfo(name = null, pickedPhotoUri = null, dateOfBirth = null)) {
            _events.trySend(EditProfileEvent.ProfileUpdated)
            return
        }

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = profileRepository.updateUserInfo(update)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(isSaving = false, newFullName = null, newDateOfBirth = null, pickedPhotoUri = null)
                    }
                    _events.send(EditProfileEvent.ProfileUpdated)
                }

                is Result.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(
                        EditProfileEvent.ProfileUpdateError(
                            error = result.error.asUiText(),
                            canRetry = result.error != DataError.PhotoTooLarge
                        )
                    )
                }
            }
        }
    }

    private fun FullNameError.asUiText(): UiText = when (this) {
        FullNameError.BLANK -> UiText.StringResource(R.string.full_name_required)
        FullNameError.TOO_LONG -> UiText.StringResource(R.string.full_name_too_long, arrayOf(MAX_FULL_NAME_LENGTH))
    }
}
