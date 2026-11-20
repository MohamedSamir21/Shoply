package com.shopping.shoply.changephone.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.usecase.countryinfo.GetCountryInfoUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePhoneUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.changephone.domain.PhoneChangeRepository
import com.shopping.shoply.changephone.domain.PhoneCodeEvent
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
class ChangePhoneViewModel @Inject constructor(
    private val repository: PhoneChangeRepository,
    private val validatePhone: ValidatePhoneUseCase,
    private val getCountries: GetCountryInfoUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(
        ChangePhoneState(currentPhone = repository.currentPhone(), needsPassword = repository.needsPassword())
    )
    val state = _state.asStateFlow()
    private val _events = Channel<ChangePhoneEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var sending: Job? = null
    private var countdown: Job? = null

    init {
        viewModelScope.launch {
            val countries = getCountries()
            _state.update { it.copy(countries = countries, shownCountries = countries, selectedCountry = countries.firstOrNull()) }
        }
    }

    fun onIntent(intent: ChangePhoneIntent) {
        when (intent) {
            is ChangePhoneIntent.ChangeNumber -> _state.update { it.copy(number = intent.number, numberError = null) }
            is ChangePhoneIntent.ChangePassword -> _state.update { it.copy(password = intent.password, passwordError = null) }
            is ChangePhoneIntent.ChangeCode ->
                _state.update { it.copy(code = intent.code.filter(Char::isDigit).take(CODE_LENGTH), codeError = null) }
            ChangePhoneIntent.ToggleCountryMenu -> _state.update { it.copy(showCountryMenu = !it.showCountryMenu, countryKeyword = "", shownCountries = it.countries) }
            is ChangePhoneIntent.SearchCountry -> _state.update { current ->
                current.copy(
                    countryKeyword = intent.keyword,
                    shownCountries = current.countries.filter { it.name.startsWith(intent.keyword.trim(), ignoreCase = true) }
                )
            }
            is ChangePhoneIntent.SelectCountry -> _state.update { it.copy(selectedCountry = intent.country, showCountryMenu = false) }
            ChangePhoneIntent.SendCode -> sendCode()
            ChangePhoneIntent.Verify -> verify()
            ChangePhoneIntent.ResendCode -> resend()
            ChangePhoneIntent.UseDifferentNumber -> {
                sending?.cancel()
                countdown?.cancel()
                _state.update { it.copy(step = ChangePhoneStep.ENTER, code = "", codeError = null, resendSecondsLeft = 0) }
            }
        }
    }

    private fun sendCode() {
        val current = state.value
        if (current.isBusy) return
        val e164 = "+" + ((current.selectedCountry?.codeNumber ?: "") + current.number).filter(Char::isDigit)
        val numberError = when {
            validatePhone(e164) is Result.Error -> UiText.StringResource(R.string.enter_valid_phone_number)
            e164 == current.currentPhone -> UiText.StringResource(R.string.new_phone_same_as_current)
            else -> null
        }
        val passwordError =
            if (current.needsPassword && current.password.isEmpty()) UiText.StringResource(R.string.password_required) else null
        if (numberError != null || passwordError != null) {
            _state.update { it.copy(numberError = numberError, passwordError = passwordError) }
            return
        }

        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            if (current.needsPassword) {
                val check = repository.confirmPassword(current.password)
                // The password has done its job either way: don't keep it around.
                _state.update { it.copy(password = "") }
                if (check is Result.Error) {
                    _state.update { it.copy(isBusy = false) }
                    showError(check.error)
                    return@launch
                }
            }
            _state.update { it.copy(sentTo = e164) }
            startSending(e164)
        }
    }

    private fun startSending(e164: String) {
        sending?.cancel()
        sending = viewModelScope.launch {
            repository.sendCode(e164).collect { event ->
                when (event) {
                    PhoneCodeEvent.CodeSent -> {
                        _state.update { it.copy(step = ChangePhoneStep.CODE, isBusy = false, code = "") }
                        startCountdown()
                    }
                    is PhoneCodeEvent.Finished -> when (val result = event.result) {
                        is Result.Success -> done()
                        is Result.Error -> {
                            _state.update { it.copy(isBusy = false) }
                            showError(result.error)
                        }
                    }
                }
            }
        }
    }

    private fun verify() {
        val current = state.value
        if (current.isBusy) return
        if (current.code.length != CODE_LENGTH) {
            _state.update { it.copy(codeError = UiText.StringResource(R.string.enter_the_code)) }
            return
        }
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            when (val result = repository.verifyCode(current.code)) {
                is Result.Success -> done()
                is Result.Error -> {
                    _state.update { it.copy(isBusy = false) }
                    showError(result.error)
                }
            }
        }
    }

    private fun resend() {
        val current = state.value
        if (current.resendSecondsLeft > 0 || current.isBusy || current.sentTo.isEmpty()) return
        _state.update { it.copy(isBusy = true, code = "", codeError = null) }
        viewModelScope.launch { _events.send(ChangePhoneEvent.Message(UiText.StringResource(R.string.code_sent_again))) }
        startSending(current.sentTo)
    }

    private fun done() {
        sending?.cancel()
        countdown?.cancel()
        _state.update { it.copy(step = ChangePhoneStep.UPDATED, isBusy = false, code = "", resendSecondsLeft = 0) }
    }

    /** Field errors go next to their field; anything else is a snackbar (FR-043, FR-044). */
    private suspend fun showError(error: DataError) {
        when (error) {
            DataError.Authentication.WRONG_PASSWORD ->
                _state.update { it.copy(step = ChangePhoneStep.ENTER, passwordError = error.asUiText()) }
            DataError.Authentication.INVALID_PHONE_NUMBER, DataError.Authentication.PHONE_NUMBER_ALREADY_IN_USE ->
                _state.update { it.copy(step = ChangePhoneStep.ENTER, numberError = error.asUiText()) }
            DataError.Authentication.INVALID_CODE, DataError.Authentication.CODE_EXPIRED ->
                _state.update { it.copy(codeError = error.asUiText()) }
            DataError.Authentication.REQUIRES_RECENT_LOGIN -> {
                // The account's sign-in is too old for this change: confirm the password again,
                // or, without a password, sign in again.
                _state.update { it.copy(step = ChangePhoneStep.ENTER, code = "") }
                val message = if (state.value.needsPassword) R.string.requires_recent_login else R.string.sign_in_again_to_change_phone
                _events.send(ChangePhoneEvent.Message(UiText.StringResource(message)))
            }
            else -> _events.send(ChangePhoneEvent.Message(error.asUiText()))
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

    private companion object {
        const val CODE_LENGTH = 6
        const val RESEND_WAIT_SECONDS = 30
    }
}
