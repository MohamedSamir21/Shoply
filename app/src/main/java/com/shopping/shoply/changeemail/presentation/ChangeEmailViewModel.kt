package com.shopping.shoply.changeemail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.changeemail.domain.EmailChangeRepository
import com.shopping.shoply.changeemail.domain.EmailChangeStatus
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangeEmailViewModel @Inject constructor(
    private val repository: EmailChangeRepository,
    private val validateEmail: ValidateEmailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(ChangeEmailState(currentEmail = repository.currentEmail().orEmpty()))
    val state = _state.asStateFlow()
    private val _events = Channel<ChangeEmailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var countdown: Job? = null

    init {
        // Coming back to a change already in progress opens on "Check your inbox".
        val askForPassword = savedStateHandle.get<Boolean>(ARG_ASK_FOR_PASSWORD) == true
        viewModelScope.launch {
            val pending = repository.pendingEmail() ?: return@launch
            if (askForPassword) {
                // The password check expired: ask again, with the pending address filled in.
                _state.update { it.copy(newEmail = pending) }
                _events.send(ChangeEmailEvent.Info(DataError.Authentication.REQUIRES_RECENT_LOGIN.asUiText()))
            } else {
                _state.update { it.copy(step = ChangeEmailStep.CHECK_INBOX, newEmail = pending) }
            }
        }
    }

    fun onIntent(intent: ChangeEmailIntent) {
        when (intent) {
            is ChangeEmailIntent.ChangeNewEmail -> _state.update { it.copy(newEmail = intent.email, emailError = null) }
            is ChangeEmailIntent.ChangePassword -> _state.update { it.copy(password = intent.password, passwordError = null) }
            ChangeEmailIntent.SendLink -> sendLink()
            ChangeEmailIntent.ResendLink -> resendLink()
            ChangeEmailIntent.UseDifferentEmail -> {
                countdown?.cancel()
                _state.update { it.copy(step = ChangeEmailStep.ENTER, password = "", resendSecondsLeft = 0) }
                // Firebase can't take back a link it already emailed (see EmailChangeRepository.cancelEmailChange).
                _events.trySend(ChangeEmailEvent.Info(UiText.StringResource(R.string.earlier_link_still_valid)))
            }
            ChangeEmailIntent.CheckIfConfirmed -> checkIfConfirmed()
        }
    }

    private fun sendLink() {
        val current = state.value
        if (current.isSending) return
        val newEmail = current.newEmail.trim()
        val emailError = when {
            validateEmail(newEmail) is Result.Error -> UiText.StringResource(R.string.enter_valid_email)
            newEmail.equals(current.currentEmail, ignoreCase = true) -> UiText.StringResource(R.string.new_email_same_as_current)
            else -> null
        }
        val passwordError = if (current.password.isEmpty()) UiText.StringResource(R.string.password_required) else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            when (val result = repository.requestEmailChange(newEmail, current.password)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(step = ChangeEmailStep.CHECK_INBOX, newEmail = newEmail, password = "", isSending = false)
                    }
                    startCountdown()
                }
                is Result.Error -> {
                    _state.update { it.copy(isSending = false) }
                    showError(result.error)
                }
            }
        }
    }

    private fun resendLink() {
        val current = state.value
        if (current.resendSecondsLeft > 0 || current.isSending) return
        _state.update { it.copy(isSending = true) }
        viewModelScope.launch {
            val result = repository.resendEmailChangeLink(current.newEmail)
            _state.update { it.copy(isSending = false) }
            when (result) {
                is Result.Success -> {
                    _events.send(ChangeEmailEvent.LinkResent)
                    startCountdown()
                }
                is Result.Error -> {
                    if (result.error == DataError.Authentication.REQUIRES_RECENT_LOGIN) {
                        // The password check expired: ask for it again on the first step.
                        _state.update { it.copy(step = ChangeEmailStep.ENTER, password = "") }
                    }
                    _events.send(ChangeEmailEvent.Error(result.error.asUiText()))
                }
            }
        }
    }

    private fun checkIfConfirmed() {
        val current = state.value
        if (current.step != ChangeEmailStep.CHECK_INBOX) return
        viewModelScope.launch {
            val result = repository.checkEmailChange(current.newEmail)
            if (result !is Result.Success) return@launch // Still waiting as far as we know; try on next resume.
            when (result.data) {
                EmailChangeStatus.WAITING -> Unit
                EmailChangeStatus.CONFIRMED -> confirm(mustSignInAgain = false)
                EmailChangeStatus.SIGNED_OUT -> confirm(mustSignInAgain = true)
            }
        }
    }

    private fun confirm(mustSignInAgain: Boolean) {
        countdown?.cancel()
        _state.update { it.copy(step = ChangeEmailStep.UPDATED, mustSignInAgain = mustSignInAgain, resendSecondsLeft = 0) }
    }

    /** Field errors go next to their field; anything else is a snackbar. */
    private suspend fun showError(error: DataError) {
        when (error) {
            DataError.Authentication.WRONG_PASSWORD ->
                _state.update { it.copy(passwordError = error.asUiText()) }
            DataError.Authentication.EMAIL_ALREADY_IN_USE, DataError.Authentication.INVALID_EMAIL ->
                _state.update { it.copy(emailError = error.asUiText()) }
            else -> _events.send(ChangeEmailEvent.Error(error.asUiText()))
        }
    }

    private fun startCountdown() {
        countdown?.cancel()
        countdown = viewModelScope.launch {
            for (seconds in RESEND_WAIT_SECONDS downTo 1) {
                _state.update { it.copy(resendSecondsLeft = seconds) }
                delay(1_000)
            }
            _state.update { it.copy(resendSecondsLeft = 0) }
        }
    }

    companion object {
        /** Navigation argument: open on the password step. */
        const val ARG_ASK_FOR_PASSWORD = "ask_password"
        private const val RESEND_WAIT_SECONDS = 30
    }
}
