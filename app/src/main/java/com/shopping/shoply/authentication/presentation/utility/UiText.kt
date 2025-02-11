package com.shopping.shoply.authentication.presentation.utility

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.shopping.shoply.R
import com.shopping.shoply.common.domain.DataError

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(
        @StringRes val id: Int,
        val args: Array<Any> = arrayOf()
    ) : UiText()

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> LocalContext.current.getString(id, *args)
        }
    }

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(id, *args)
        }
    }
}

fun DataError.asUiText(): UiText {
    return when (this) {
        DataError.Authentication.WEAK_PASSWORD -> UiText.StringResource(R.string.weak_password)
        DataError.Authentication.WRONG_PASSWORD -> UiText.StringResource(R.string.wrong_password)
        DataError.Authentication.INVALID_EMAIL -> UiText.StringResource(R.string.enter_valid_email)
        DataError.Authentication.USER_DISABLED -> UiText.StringResource(R.string.disabled_user)
        DataError.Authentication.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL -> UiText.StringResource(R.string.account_with_different_credentials)
        DataError.Authentication.EMAIL_ALREADY_IN_USE -> UiText.StringResource(R.string.already_email_exits)
        DataError.Authentication.USER_COLLISION -> UiText.StringResource(R.string.already_email_exits)
        DataError.Authentication.NOT_VERIFIED -> UiText.StringResource(R.string.cannot_verify_you)
        DataError.Authentication.ALREADY_VERIFIED -> UiText.StringResource(R.string.already_verified_message)
        DataError.Authentication.UNKNOWN -> UiText.StringResource(R.string.unknown_authentication_error)
        DataError.Network -> UiText.StringResource(R.string.network_issue)
        DataError.Unknown -> UiText.StringResource(R.string.unknown_error)
    }
}