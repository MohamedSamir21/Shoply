package com.shopping.shoply.authentication.presentation.reset_password.forgot_password

import android.util.Log
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

private const val TAG = "ForgotPasswordViewModel"
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase
): ViewModel() {
    private val _state = MutableStateFlow(ForgotPasswordScreenState())
    val state = _state.onStart {
        getUserInfo()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ForgotPasswordScreenState()
    )

    fun onSelectRestMethod() {
        _state.update {
            it.copy(
                selectedRestMethodIndex = if (state.value.selectedRestMethodIndex == 0) 1 else 0
            )
        }
    }

    private fun getUserInfo() {
        viewModelScope.launch {
            when(val userInfo = getUserInfoUseCase()) {
                is Result.Error -> Unit
                is Result.Success -> {
                    val mutableList = state.value.restMethods.toMutableList()
                    mutableList[0] = mutableList[0].copy(
                        message = hidePartOfEmailWithStars(userInfo.data.email)
                    )
                    mutableList[1] = mutableList[1].copy(
                        message = hidePartOfPhoneNumberWithStars(userInfo.data.phoneNumber)
                    )
                    _state.update {
                        it.copy(
                            restMethods = mutableList
                        )
                    }
                }
            }
        }
    }
}