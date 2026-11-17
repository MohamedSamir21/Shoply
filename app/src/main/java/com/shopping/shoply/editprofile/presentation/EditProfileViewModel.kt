package com.shopping.shoply.editprofile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            profileRepository.observeAccount().first().let { account ->
                _state.update {
                    it.copy(
                        photoUrl = account.photoUrl,
                        customPhoto = account.customPhoto,
                        fullName = account.name,
                        email = account.email,
                        dateOfBirth = account.dateOfBirth,
                        phoneNumber = account.phone
                    )
                }
            }
        }
    }

    fun onIntent(intent: EditProfileIntent) = when (intent) {
        is EditProfileIntent.ChangeAvatar -> {
            _state.update { it.copy(pickedPhotoUri = intent.uri) }
        }

        is EditProfileIntent.ChangeDateOfBirth -> {
            _state.update { it.copy(dateOfBirth = intent.dateOfBirth) }
        }

        is EditProfileIntent.ChangeFullName -> {
            _state.update { it.copy(newFullName = intent.newName) }
        }

        EditProfileIntent.SaveUserInfo -> {
            _state.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                val cachedStateValue = state.value
                val result = profileRepository.updateUserInfo(
                    userInfo = UserInfo(
                        name = cachedStateValue.newFullName,
                        pickedPhotoUri = cachedStateValue.pickedPhotoUri,
                        dateOfBirth = cachedStateValue.dateOfBirth
                    )
                )
                when (result) {
                    is Result.Success -> {
                        _events.send(EditProfileEvent.ProfileUpdated)
                    }

                    is Result.Error -> {
                        _events.send(EditProfileEvent.ProfileUpdateError(result.error.asUiText()))
                    }
                }
                _state.update { it.copy(isLoading = false) }
            }
        }
    }


}