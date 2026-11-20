package com.shopping.shoply.changeemail.presentation

import com.shopping.shoply.authentication.presentation.utility.UiText

enum class ChangeEmailStep { ENTER, CHECK_INBOX, UPDATED }

/**
 * [newEmail] is what the user typed on [ChangeEmailStep.ENTER], then the address the link went to.
 * [mustSignInAgain] is true when the session ended (FR-038); the email may or may not have changed.
 */
data class ChangeEmailState(
    val step: ChangeEmailStep = ChangeEmailStep.ENTER,
    val currentEmail: String = "",
    val newEmail: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val isSending: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val mustSignInAgain: Boolean = false
) {
    // Data class toString would print the password into any log or crash report.
    override fun toString() = "ChangeEmailState(step=$step, newEmail=$newEmail, isSending=$isSending)"
}

sealed interface ChangeEmailIntent {
    data class ChangeNewEmail(val email: String) : ChangeEmailIntent
    data class ChangePassword(val password: String) : ChangeEmailIntent
    data object SendLink : ChangeEmailIntent
    data object ResendLink : ChangeEmailIntent
    data object UseDifferentEmail : ChangeEmailIntent

    /** The user came back to the app: they may have clicked the link meanwhile. */
    data object CheckIfConfirmed : ChangeEmailIntent
}

sealed interface ChangeEmailEvent {
    data class Error(val message: UiText) : ChangeEmailEvent
    data class Info(val message: UiText) : ChangeEmailEvent
    data object LinkResent : ChangeEmailEvent
}
