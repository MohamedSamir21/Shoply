package com.shopping.shoply.authentication.presentation.resetpassword.createpassword

sealed interface CreateNewPasswordIntent {
    data class ChangePassword(val password: String) : CreateNewPasswordIntent
    data class ChangeConfirmedPassword(val confirmedPassword: String) : CreateNewPasswordIntent
    data class ChangePasswordVisibility(val isVisible: Boolean) : CreateNewPasswordIntent
    data object Submit : CreateNewPasswordIntent
}
