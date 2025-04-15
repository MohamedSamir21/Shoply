package com.shopping.shoply.authentication.presentation.reset_password.enter_phone

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.usecase.auth.CheckIfPhoneNumberExistsUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SendOTPCodeUseCase
import com.shopping.shoply.authentication.domain.usecase.get_country_info.GetCountryInfoUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePhoneUseCase
import com.shopping.shoply.authentication.presentation.model.ActivityContext
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

private const val TAG = "EnterPhoneViewModel"

@HiltViewModel
class EnterPhoneViewModel @Inject constructor(
    private val validatePhoneUseCase: ValidatePhoneUseCase,
    private val checkIfPhoneNumberExistsUseCase: CheckIfPhoneNumberExistsUseCase,
    private val sendOTPCodeUseCase: SendOTPCodeUseCase,
    private val getCountryInfoUseCase: GetCountryInfoUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(EnterPhoneState())
    val state = _state.asStateFlow()

    fun loadCountriesInfo(context: Context) {
        viewModelScope.launch {
            getCountryInfoUseCase(ActivityContext(context))
                .collect { countriesInfoList ->
                    _state.update {
                        it.copy(
                            selectedCountryInfo = countriesInfoList.first(),
                            countriesInfo = countriesInfoList,
                            countriesInfoByKeyword = countriesInfoList
                        )
                    }
                }
            if(_state.value.countriesInfo.size == 1) {
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
                Log.d(TAG, intent.keyword)
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
            if (it.isLowerCase()) it.titlecase(
                Locale.US
            ) else it.toString()
        }

        if (keyword.isNotBlank()) {
            _state.update {
                it.copy(
                    countriesInfoByKeyword = state.value.countriesInfo.filter { countryInfo ->
                        countryInfo.name.startsWith(keyword)
                    }
                )
            }
            Log.d(TAG, state.value.countriesInfoByKeyword.toString())
        }
        if (keyword.isEmpty()) {
            _state.update {
                it.copy(
                    countriesInfoByKeyword = state.value.countriesInfo
                )
            }
        }
    }

    fun onNext(context: ActivityContext) {
        val validationResult = validatePhoneUseCase(state.value.phoneNumber)
        if (!validationResult.error.isNullOrEmpty()) {
            _state.update {
                it.copy(
                    error = UiText.DynamicString(validationResult.error)
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

            checkIfPhoneNumberExistsUseCase(
                phoneNumber = state.value.phoneNumber,
                onComplete = { result ->
                    when (result) {
                        is Result.Error -> {
                            _state.update {
                                it.copy(
                                    error = result.error.asUiText(),
                                    isProcessing = false
                                )
                            }
                        }

                        is Result.Success -> {
                            _state.update {
                                it.copy(
                                    error = null
                                )
                            }
                            sendOTPCode(
                                context,
                                state.value.selectedCountryInfo.code,
                                state.value.phoneNumber
                            )
                        }
                    }
                }
            )
        }
    }

    private fun sendOTPCode(
        context: ActivityContext,
        countryCode: String,
        phoneNumber: String
    ) {
        viewModelScope.launch {
            sendOTPCodeUseCase(
                domainContext = context,
                countryCode = countryCode,
                phoneNumber = phoneNumber,
                onSent = {
                    viewModelScope.launch {
                        _phoneVerificationResult.send(EnterPhoneEvent.Success)
                    }
                },
                onAutoVerification = { result ->
                    when (result) {
                        is Result.Error -> {
                            _state.update {
                                it.copy(
                                    error = result.error.asUiText(),
                                    isProcessing = false
                                )
                            }
                        }

                        is Result.Success -> {
                            _state.update {
                                it.copy(
                                    isProcessing = false,
                                    alreadyVerified = true
                                )
                            }
                            viewModelScope.launch {
                                _phoneVerificationResult.send(EnterPhoneEvent.Success)
                            }
                        }
                    }
                }
            )
        }
    }
}