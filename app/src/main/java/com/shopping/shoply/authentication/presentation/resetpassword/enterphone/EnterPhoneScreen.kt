package com.shopping.shoply.authentication.presentation.resetpassword.enterphone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.presentation.components.CountryCodePicker
import com.shopping.shoply.authentication.presentation.components.EnterMethodComponent
import com.shopping.shoply.authentication.presentation.resetpassword.otpverification.OtpPurpose
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import kotlinx.coroutines.flow.collectLatest


@Composable
fun EnterPhoneScreen(
    navController: NavController,
    isExpandedScreen: Boolean
) {
    val viewModel: EnterPhoneViewModel = hiltViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        viewModel.loadCountriesInfo()
    }
    LaunchedEffect(viewModel.phoneVerificationResult) {
        viewModel.phoneVerificationResult.collectLatest { event ->
            navController.popBackStack()
            navController.navigate(
                NavRoutes.OTPCodeVerification.route
                    .replace(
                        oldValue = "{${NavRoutes.OTPCodeVerification.Args.COUNTRY_CODE}}",
                        newValue = event.countryCode
                    )
                    .replace(
                        oldValue = "{${NavRoutes.OTPCodeVerification.Args.PHONE_NUMBER_WITH_COUNTRY_CODE}}",
                        newValue = event.phoneNumber
                    )
                    .replace(
                        oldValue = "{${NavRoutes.OTPCodeVerification.Args.PURPOSE}}",
                        newValue = OtpPurpose.RESET.name
                    )
            )
        }
    }

    EnterPhoneContent(
        isExpandedScreen = isExpandedScreen,
        methodValue = state.phoneNumber,
        isVerifyingValue = state.isProcessing,
        selectedCountryInfo = state.selectedCountryInfo,
        countriesInfo = state.countriesInfoByKeyword,
        errorMessage = state.error,
        showCountryInfoMenu = state.showCountryInfoMenu,
        isCountryCodeSearchButtonClickable = state.isCountryCodeSearchButtonClickable,
        keyword = state.keyword,
        onPhoneNumberChange = { viewModel.onChangePhone(it) },
        onBack = { navController.popBackStack() },
        onNext = { viewModel.onNext() },
        onToggleCountryMenu = { viewModel.onIntent(EnterPhoneIntent.ToggleCountryInfoMenuVisibility) },
        onKeywordChange = { viewModel.onIntent(EnterPhoneIntent.ChangeSearchKeyword(it)) },
        onSelectCountry = { viewModel.onIntent(EnterPhoneIntent.SelectCountryInfo(it)) }
    )
}

@Composable
private fun EnterPhoneContent(
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean,
    methodName: String = stringResource(R.string.phone_number),
    methodMessage: String = stringResource(R.string.enter_phone_guiding_message),
    methodValue: String,
    isVerifyingValue: Boolean,
    isCountryCodeSearchButtonClickable: Boolean,
    selectedCountryInfo: CountryInfo,
    countriesInfo: List<CountryInfo>,
    errorMessage: UiText? = null,
    inputLabel: String = stringResource(R.string.phone_number),
    keyword: String,
    showCountryInfoMenu: Boolean,
    onKeywordChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onToggleCountryMenu: () -> Unit,
    onSelectCountry: (CountryInfo) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    EnterMethodComponent(
        modifier = modifier,
        isExpandedScreen = isExpandedScreen,
        methodName = methodName,
        methodMessage = methodMessage,
        methodValue = methodValue,
        isVerifyingValue = isVerifyingValue,
        errorMessage = errorMessage ?: UiText.StringResource(R.string.standard_rates_apply),
        inputLabel = inputLabel,
        onMethodValueChange = onPhoneNumberChange,
        onBack = onBack,
        onNext = onNext,
        menuButton = {
            Column{
                CountryCodePicker(
                    selectedCountryInfo = selectedCountryInfo,
                    countries = countriesInfo,
                    keyword = keyword,
                    showMenu = showCountryInfoMenu,
                    isClickable = isCountryCodeSearchButtonClickable,
                    onToggleMenu = onToggleCountryMenu,
                    onKeywordChange = onKeywordChange,
                    onSelect = onSelectCountry,
                    onDismiss = onToggleCountryMenu
                )
                Spacer(Modifier.height(ShoplyDimens.space2))
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Go
        )
    )
}

@Preview
@Composable
private fun EnterPhoneContentPreview() {
    ShoplyTheme {
        EnterPhoneContent(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            isExpandedScreen = true,
            methodValue = "Phone Number",
            isVerifyingValue = false,
            isCountryCodeSearchButtonClickable = true,
            onPhoneNumberChange = {},
            onBack = {},
            onNext = {},
            countriesInfo = emptyList(),
            selectedCountryInfo = CountryInfo("", "", "", ""),
            showCountryInfoMenu = false,
            keyword = "",
            onKeywordChange = {},
            onToggleCountryMenu = {},
            onSelectCountry = {}
        )
    }
}
