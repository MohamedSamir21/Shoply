package com.shopping.shoply.authentication.presentation.reset_password.forgot_password

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ForgotPasswordViewModel : ViewModel() {
    private val _state = MutableStateFlow(ForgotPasswordScreenState())
    val state = _state.asStateFlow()

    fun onSelectRestMethod() {
        _state.update {
            it.copy(
                selectedRestMethodIndex = if (state.value.selectedRestMethodIndex == 0) 1 else 0
            )
        }
    }
}