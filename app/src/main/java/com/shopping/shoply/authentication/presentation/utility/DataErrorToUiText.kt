package com.shopping.shoply.authentication.presentation.utility

import com.shopping.shoply.R
import com.shopping.shoply.common.domain.DataError

fun DataError.asUiText(): UiText {
    return when (this) {
        DataError.Authentication.WEAK_PASSWORD -> UiText.StringResource(R.string.weak_password)
        DataError.Authentication.WRONG_PASSWORD -> UiText.StringResource(R.string.wrong_password)
        DataError.Authentication.INVALID_EMAIL -> UiText.StringResource(R.string.enter_valid_email)
        DataError.Authentication.EMAIL_DOES_NOT_EXIST -> UiText.StringResource(R.string.email_does_not_exist)
        DataError.Authentication.PHONE_NUMBER_DOES_NOT_EXIST -> UiText.StringResource(R.string.phone_number_does_not_exist)
        DataError.Authentication.PHONE_NUMBER_NOT_LINKED -> UiText.StringResource(R.string.phone_number_not_linked)
        DataError.Authentication.USER_DISABLED -> UiText.StringResource(R.string.disabled_user)
        DataError.Authentication.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL -> UiText.StringResource(R.string.account_with_different_credentials)
        DataError.Authentication.EMAIL_ALREADY_IN_USE -> UiText.StringResource(R.string.already_email_exits)
        DataError.Authentication.USER_COLLISION -> UiText.StringResource(R.string.already_email_exits)
        DataError.Authentication.PHONE_NUMBER_ALREADY_IN_USE -> UiText.StringResource(R.string.phone_already_in_use)
        DataError.Authentication.NOT_VERIFIED -> UiText.StringResource(R.string.cannot_verify_you)
        DataError.Authentication.ALREADY_VERIFIED -> UiText.StringResource(R.string.already_verified_message)
        DataError.Authentication.INVALID_CREDENTIALS -> UiText.StringResource(R.string.invalid_credentials)
        DataError.Authentication.UNKNOWN -> UiText.StringResource(R.string.unknown_authentication_error)
        DataError.Network -> UiText.StringResource(R.string.network_issue)
        DataError.Unknown -> UiText.StringResource(R.string.unknown_error)
    }
}