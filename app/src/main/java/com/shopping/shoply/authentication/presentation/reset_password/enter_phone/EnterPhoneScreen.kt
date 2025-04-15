package com.shopping.shoply.authentication.presentation.reset_password.enter_phone

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.presentation.components.EnterMethodComponent
import com.shopping.shoply.authentication.presentation.model.ActivityContext
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import kotlinx.coroutines.flow.collectLatest


@Composable
fun EnterPhoneScreen(
    navController: NavController,
    isExpandedScreen: Boolean
) {
    val viewModel: EnterPhoneViewModel = hiltViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadCountriesInfo(context)
    }
    LaunchedEffect(viewModel.phoneVerificationResult) {
        viewModel.phoneVerificationResult.collectLatest {
            navController.popBackStack()
            navController.navigate(
                NavRoutes.OTPCodeVerification.route.replace(
                    oldValue = "already_verified",
                    newValue = state.alreadyVerified.toString()
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
        onPhoneNumberChange = {
            viewModel.onChangePhone(it)
        },
        onBack = {
            navController.popBackStack()
        },
        onNext = {
            viewModel.onNext(ActivityContext(context))
        },
        onToggleDropdownMenuVisibility = {
            viewModel.onIntent(it)
        },
        keyword = state.keyword,
        onKeywordChange = {
            viewModel.onIntent(EnterPhoneIntent.ChangeSearchKeyword(it))
        },
        onSelectCountry = {
            viewModel.onIntent(EnterPhoneIntent.SelectCountryInfo(it))
        },
        onCancelCountryInfoMenu = {
            viewModel.onIntent(EnterPhoneIntent.ToggleCountryInfoMenuVisibility)
        }
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
    onKeywordChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onCancelCountryInfoMenu: () -> Unit,
    onSelectCountry: (CountryInfo) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onToggleDropdownMenuVisibility: (EnterPhoneIntent) -> Unit = {},
    showCountryInfoMenu: Boolean
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (showCountryInfoMenu) {
            Scaffold(
                topBar = {
                    // Implement Search TextField
                    SearchComponent(
                        keyword,
                        onKeywordChange,
                        onCancelCountryInfoMenu
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(innerPadding)
                ) {
                    items(
                        items = countriesInfo,
                        key = {
                            it.name
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCountry(it) }
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                Text(
                                    text = it.flag
                                )
                                Spacer(Modifier.width(20.dp))
                                Text(
                                    text = it.name
                                )
                            }
                            Text(
                                text = it.code
                            )
                        }
                    }
                }
            }
        }

        if (!showCountryInfoMenu) {
            EnterMethodComponent(
                isExpandedScreen = isExpandedScreen,
                methodName = methodName,
                methodMessage = methodMessage,
                methodValue = methodValue,
                isVerifyingValue = isVerifyingValue,
                errorMessage = errorMessage ?: UiText.StringResource(
                    R.string.standard_rates_apply
                ),
                inputLabel = inputLabel,
                onMethodValueChange = {
                    onPhoneNumberChange(it)
                },
                onBack = onBack,
                onNext = onNext,
                menuButton = {
                    CountryInfoMenuDisplayButton(
                        onToggleDropdownMenuVisibility,
                        selectedCountryInfo,
                        isCountryCodeSearchButtonClickable
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Go
                )
            )
        }
    }
}

@Composable
private fun SearchComponent(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onCancelCountryInfoMenu: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .padding(10.dp)
                .height(50.dp)
                .weight(1f),
            value = keyword,
            onValueChange = {
                onKeywordChange(it)
            },
            supportingText = null,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            },
            shape = CircleShape
        )
        TextButton(
            onClick = onCancelCountryInfoMenu
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}

@Composable
private fun CountryInfoMenuDisplayButton(
    onToggleDropdownMenuVisibility: (EnterPhoneIntent) -> Unit,
    selectedCountryInfo: CountryInfo,
    isClickable: Boolean = true
) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .size(100.dp, 56.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.onSurface,
                RoundedCornerShape(3.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable {
                if (isClickable) {
                    onToggleDropdownMenuVisibility(EnterPhoneIntent.ToggleCountryInfoMenuVisibility)
                }
            }
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedCountryInfo.flag,
            textAlign = TextAlign.Center
        )
        Text(
            text = selectedCountryInfo.code,
            textAlign = TextAlign.Center
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = stringResource(R.string.down_arrow)
        )
    }
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
            onCancelCountryInfoMenu = {},
            onSelectCountry = {}
        )
    }
}



