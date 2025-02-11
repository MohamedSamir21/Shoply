package com.shopping.shoply.authentication.presentation.screen.verify_result

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.usecase.auth.VerifyEmailUseCase
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "VerificationResultViewModel"

@HiltViewModel
class VerificationResultViewModel @Inject constructor(
    private val verifyEmailUseCase: VerifyEmailUseCase
): ViewModel() {
    private val _state = MutableStateFlow(VerificationResultState())
    val state = _state.asStateFlow()

    fun verifyEmail(deepLink: String) {
        viewModelScope.launch {
            when(val result = verifyEmailUseCase(deepLink)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isVerifying = false,
                            verificationHasSucceeded = true
                        )
                    }
                    Log.i(TAG, "Success!!")
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isVerifying = false,
                            verificationError = result.error.asUiText()
                        )
                    }
                    Log.i(TAG, "Error!!")

                }
            }
        }
    }
}