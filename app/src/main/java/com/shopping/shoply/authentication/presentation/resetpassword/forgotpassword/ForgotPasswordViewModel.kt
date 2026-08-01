package com.shopping.shoply.authentication.presentation.resetpassword.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.usecase.auth.GetUserInfoUseCase
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.presentation.util.hidePartOfEmailWithStars
import com.shopping.shoply.common.presentation.util.hidePartOfPhoneNumberWithStars
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Backs the forgot-password method picker, offering the user's masked email and phone number as
 * reset options. [state] is shared with [SharingStarted.Lazily], so the user info loads on first
 * collection rather than at construction; if loading fails the methods stay unmasked.
 */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ForgotPasswordScreenState())
    val state = _state.onStart {
        getUserInfo()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ForgotPasswordScreenState()
    )

    fun onSelectResetMethod() {
        _state.update {
            it.copy(
                selectedResetMethodIndex = if (state.value.selectedResetMethodIndex == 0) 1 else 0
            )
        }
    }

    private fun getUserInfo() {
        viewModelScope.launch {
            when (val userInfo = getUserInfoUseCase()) {
                is Result.Error -> Unit
                is Result.Success -> {
                    val email = userInfo.data.email
                    val phoneNumber = userInfo.data.phoneNumber

                    val mutableList = state.value.resetMethods.toMutableList()
                    email?.let {
                        mutableList[0] = mutableList[0].copy(
                            message = hidePartOfEmailWithStars(it),
                        )
                    }
                    phoneNumber?.let {
                        mutableList[1] = mutableList[1].copy(
                            message = hidePartOfPhoneNumberWithStars(it)
                        )
                    }
                    _state.update {
                        it.copy(
                            resetMethods = mutableList,
                            email = email,
                            phoneNumber = phoneNumber,
                            isPhoneVerified = userInfo.data.isPhoneVerified
                        )
                    }
                }
            }
        }
    }
}