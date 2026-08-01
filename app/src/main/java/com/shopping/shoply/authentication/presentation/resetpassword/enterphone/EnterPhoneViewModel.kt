package com.shopping.shoply.authentication.presentation.resetpassword.enterphone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.usecase.auth.CheckIfPhoneNumberExistsUseCase
import com.shopping.shoply.authentication.domain.usecase.countryinfo.GetCountryInfoUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePhoneUseCase
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.errorOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * Backs the enter-phone step of the password-reset flow: validates the number against the selected
 * country and confirms an account exists for it. Success is delivered as a one-shot
 * [phoneVerificationResult] event; country data must be loaded explicitly via [loadCountriesInfo].
 */
@HiltViewModel
class EnterPhoneViewModel @Inject constructor(
    private val validatePhoneUseCase: ValidatePhoneUseCase,
    private val checkIfPhoneNumberExistsUseCase: CheckIfPhoneNumberExistsUseCase,
    private val getCountryInfoUseCase: GetCountryInfoUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(EnterPhoneState())
    val state = _state.asStateFlow()

    /**
     * Loads the country list and selects its first entry. Must be called explicitly by the
     * screen (e.g. from a LaunchedEffect) — nothing loads at construction.
     */
    fun loadCountriesInfo() {
        viewModelScope.launch {
            val countriesInfoList = getCountryInfoUseCase()
            _state.update {
                it.copy(
                    selectedCountryInfo = countriesInfoList.first(),
                    countriesInfo = countriesInfoList,
                    countriesInfoByKeyword = countriesInfoList
                )
            }
            if (_state.value.countriesInfo.size == 1) {
                _state.update {
                    it.copy(
                        isCountryCodeSearchButtonClickable = false
                    )
                }
            }
        }
    }

    private val _phoneVerificationResult = Channel<EnterPhoneEvent.Success>()
    val phoneVerificationResult = _phoneVerificationResult.receiveAsFlow()

    fun onChangePhone(value: String) {
        _state.update {
            it.copy(
                phoneNumber = value
            )
        }
    }

    fun onIntent(intent: EnterPhoneIntent) {
        when (intent) {
            EnterPhoneIntent.ToggleCountryInfoMenuVisibility -> {
                _state.update {
                    it.copy(
                        showCountryInfoMenu = !state.value.showCountryInfoMenu
                    )
                }
            }

            is EnterPhoneIntent.ChangeSearchKeyword -> {
                _state.update {
                    it.copy(
                        keyword = intent.keyword
                    )
                }
                searchForKeyword()
            }

            is EnterPhoneIntent.SelectCountryInfo -> {
                _state.update {
                    it.copy(
                        selectedCountryInfo = intent.countryInfo,
                        showCountryInfoMenu = false
                    )
                }
            }
        }
    }

    private fun searchForKeyword() {
        val keyword = state.value.keyword.replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(
                    Locale.US
                )
            } else {
                it.toString()
            }
        }

        if (keyword.isNotBlank()) {
            _state.update {
                it.copy(
                    countriesInfoByKeyword = state.value.countriesInfo.filter { countryInfo ->
                        countryInfo.name.startsWith(keyword)
                    }
                )
            }
        }
        if (keyword.isEmpty()) {
            _state.update {
                it.copy(
                    countriesInfoByKeyword = state.value.countriesInfo
                )
            }
        }
    }

    fun onNext() {
        // Numbers are stored as E.164 (+<code><national>), so build that form up front: it is both
        // what we validate and what we look the user up by.
        // CountryInfo.code already carries a '+', so normalize to a single leading '+'.
        val e164 =
            "+" + (state.value.selectedCountryInfo.codeNumber + state.value.phoneNumber).filter(Char::isDigit)

        val validationError = validatePhoneUseCase(e164).errorOrNull()
        if (validationError != null) {
            _state.update {
                it.copy(
                    error = validationError.asUiText()
                )
            }
            return
        }

        // Check if phone exists
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isProcessing = true,
                    error = null
                )
            }

            when (val result = checkIfPhoneNumberExistsUseCase(e164)) {
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            error = result.error.asUiText(),
                            isProcessing = false
                        )
                    }
                }

                is Result.Success -> {
                    // The phone exists: hand off to the OTP screen, which owns sending the code.
                    // It expects E.164 digits (no '+'); it re-adds the '+' before sending.
                    _state.update {
                        it.copy(
                            error = null,
                            isProcessing = false
                        )
                    }
                    _phoneVerificationResult.send(
                        EnterPhoneEvent.Success(
                            countryCode = _state.value.selectedCountryInfo.codeNumber,
                            phoneNumber = e164.filter(Char::isDigit)
                        )
                    )
                }
            }
        }
    }
}