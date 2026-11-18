package com.shopping.shoply.editprofile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.editprofile.domain.FullNameError
import com.shopping.shoply.editprofile.domain.MAX_FULL_NAME_LENGTH
import com.shopping.shoply.editprofile.domain.validateFullName
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
    private val profileRepository: ProfileRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()
    private val _events = Channel<EditProfileEvent>()
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
                        dateOfBirth = account.dateOfBirth,
                        phoneNumber = account.phone,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onIntent(intent: EditProfileIntent) = when (intent) {
        is EditProfileIntent.ChangeAvatar -> _state.update { it.copy(pickedPhotoUri = intent.uri) }
        is EditProfileIntent.ChangeDateOfBirth -> _state.update { it.copy(newDateOfBirth = intent.dateOfBirth) }
        is EditProfileIntent.ChangeFullName -> _state.update { it.copy(newFullName = intent.newName, nameError = null) }
        EditProfileIntent.SaveUserInfo -> save()
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
