package com.shopping.shoply.authentication.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.shopping.shoply.authentication.presentation.screen.sign_up.SignUpScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SignUpViewModel: ViewModel() {
    private val _state = MutableStateFlow(SignUpScreenState())
    val state = _state.asStateFlow()


}