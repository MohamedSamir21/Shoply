package com.shopping.shoply.authentication.presentation.signup

import android.app.UiModeManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ContinueWithRow
import com.shopping.shoply.authentication.presentation.components.CountryCodePicker
import com.shopping.shoply.authentication.presentation.components.FacebookButton
import com.shopping.shoply.authentication.presentation.components.FormEmail
import com.shopping.shoply.authentication.presentation.components.FormPassword
import com.shopping.shoply.authentication.presentation.components.FormTextField
import com.shopping.shoply.authentication.presentation.components.GoogleButton
import com.shopping.shoply.authentication.presentation.components.PasswordStrengthMeter
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.components.ShoplyButtonVariant
import com.shopping.shoply.authentication.presentation.resetpassword.otpverification.OtpPurpose
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.navigation.navigateToHome
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    navHostController: NavHostController,
    isExpandedScreen: Boolean
) {
    val state = viewModel.state.collectAsState()

    LaunchedEffect(viewModel.signUpResult) {
        viewModel.signUpResult.collectLatest { result ->
            when (result) {
                is SignUpScreenEvent.SignUpResult.Success -> {
                    when (result) {
                        SignUpScreenEvent.SignUpResult.Success.NotVerified -> {
                            val countryCode = state.value.selectedCountryInfo.codeNumber
                            val phoneDigits =
                                (countryCode + state.value.form.phoneNumber)
                                    .filter(Char::isDigit)
                            navHostController.navigate(
                                NavRoutes.OTPCodeVerification.route
                                    .replace(
                                        "{${NavRoutes.OTPCodeVerification.Args.COUNTRY_CODE}}",
                                        countryCode
                                    )
                                    .replace(
                                        "{${NavRoutes.OTPCodeVerification.Args.PHONE_NUMBER_WITH_COUNTRY_CODE}}",
                                        phoneDigits
                                    )
                                    .replace(
                                        "{${NavRoutes.OTPCodeVerification.Args.PURPOSE}}",
                                        OtpPurpose.SIGNUP_LINK.name
                                    )
                            )
                        }

                        SignUpScreenEvent.SignUpResult.Success.Verified -> {
                            navHostController.navigateToHome()
                        }
                    }
                }

                is SignUpScreenEvent.SignUpResult.Failure -> {
                    viewModel.onEvent(SignUpScreenEvent.SignUpResult.Failure(result.uiText))
                }
            }
        }
    }

    SignUpContent(
        isExpandedScreen = isExpandedScreen,
        state = state.value,
        onEvent = { event ->
            if (event is SignUpScreenIntent.SignIn) {
                navHostController.navigate(NavRoutes.SignIn.route)
            } else {
                viewModel.onIntent(event)
            }
        }
    )
}

@Composable
internal fun SignUpContent(
    isExpandedScreen: Boolean,
    state: SignUpScreenState,
    onEvent: (SignUpScreenIntent) -> Unit,
    // A slot so screen tests can swap in plain buttons: the real Facebook button needs Hilt and
    // the Facebook SDK, which a JVM UI test doesn't have.
    socialButtons: @Composable (onFacebookClick: () -> Unit, onGoogleClick: () -> Unit) -> Unit =
        { onFacebookClick, onGoogleClick -> SignUpAlternativesRow(onFacebookClick, onGoogleClick) }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShoplyDimens.gutter),
    ) {
        Spacer(Modifier.height(ShoplyDimens.space5))
        Text(
            text = stringResource(R.string.create_account),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(Modifier.height(ShoplyDimens.space2))
        Text(
            text = stringResource(R.string.promo_text),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(ShoplyDimens.space5))

        Form(state, isExpandedScreen, onEvent)

        if (!isExpandedScreen) {
            Spacer(Modifier.height(ShoplyDimens.space5))
            ContinueWithRow()
            Spacer(Modifier.height(ShoplyDimens.space4))
            socialButtons(
                { onEvent(SignUpScreenIntent.SignUp.Facebook) },
                { onEvent(SignUpScreenIntent.SignUp.Google) }
            )
            AlreadyHaveAccountRow(
                onSignInClicked = { onEvent(SignUpScreenIntent.SignIn) }
            )
            Spacer(Modifier.height(ShoplyDimens.space5))
        }
    }
}

@Composable
private fun Form(
    state: SignUpScreenState,
    isExpandedScreen: Boolean,
    onEvent: (SignUpScreenIntent) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val signUpError = state.signUpError?.asString()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FormTextFields(
            modifier = if (isExpandedScreen) Modifier.wrapContentWidth() else Modifier.fillMaxWidth(),
            state = state,
            onEvent = onEvent
        )
        signUpError?.let {
            Spacer(Modifier.height(ShoplyDimens.space2))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(Modifier.height(ShoplyDimens.space5))
        if (state.isSigningUp) {
            CircularProgressIndicator()
        } else {
            ShoplyButton(
                label = stringResource(R.string.sign_up),
                modifier = if (isExpandedScreen) {
                    Modifier.wrapContentWidth().focusRequester(focusRequester)
                } else {
                    Modifier.fillMaxWidth().focusRequester(focusRequester)
                },
                onClick = {
                    focusRequester.requestFocus()
                    onEvent(SignUpScreenIntent.SignUp.SubmitForm)
                },
                enable = state.enableSignUp
            )
        }
    }
}

@Composable
private fun FormTextFields(
    modifier: Modifier = Modifier,
    state: SignUpScreenState,
    onEvent: (SignUpScreenIntent) -> Unit
) {
    FormUsername(
        modifier = modifier,
        username = state.form.username,
        onUsernameChanged = { onEvent(SignUpScreenIntent.Change.Username(it)) },
        errorMessage = state.userError.usernameError?.asString(),
        onClear = { onEvent(SignUpScreenIntent.Clear.Username) }
    )
    FormEmail(
        modifier = modifier,
        email = state.form.email,
        onEmailChanged = { onEvent(SignUpScreenIntent.Change.Email(it)) },
        errorMessage = state.userError.emailError?.asString(),
        onClear = { onEvent(SignUpScreenIntent.Clear.Email) }
    )
    // Matches the design: the country picker lives *inside* the phone field as its
    // leading element (with a divider), so the field is the very same control as the
    // others and is guaranteed to share their height.
    FormPhoneNumber(
        modifier = modifier,
        phoneNumber = state.form.phoneNumber,
        onPhoneNumberChanged = { onEvent(SignUpScreenIntent.Change.PhoneNumber(it)) },
        onPhoneNumberCleared = { onEvent(SignUpScreenIntent.Clear.PhoneNumber) },
        errorMessage = state.userError.phoneNumberError?.asString(),
        countryLeading = {
            Row(
                modifier = Modifier.padding(start = ShoplyDimens.space3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CountryCodePicker(
                    bare = true,
                    selectedCountryInfo = state.selectedCountryInfo,
                    countries = state.countriesInfoByKeyword,
                    keyword = state.countryKeyword,
                    showMenu = state.showCountryInfoMenu,
                    isClickable = state.isCountryCodeSearchButtonClickable,
                    onToggleMenu = { onEvent(SignUpScreenIntent.Country.ToggleMenu) },
                    onKeywordChange = { onEvent(SignUpScreenIntent.Country.ChangeKeyword(it)) },
                    onSelect = { onEvent(SignUpScreenIntent.Country.Select(it)) },
                    onDismiss = { onEvent(SignUpScreenIntent.Country.ToggleMenu) }
                )
                Spacer(Modifier.width(ShoplyDimens.space2))
                Box(
                    Modifier
                        .height(26.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    )
    FormPassword(
        modifier = modifier,
        password = state.form.password,
        onPasswordChanged = { onEvent(SignUpScreenIntent.Change.Password(it)) },
        onVisibilityChanged = { onEvent(SignUpScreenIntent.Change.PasswordVisibility(it)) },
        showPassword = state.showPassword,
        errorMessage = state.userError.passwordError?.asString(),
    )
    PasswordStrengthMeter(
        strength = state.passwordStrength,
        modifier = modifier.padding(top = ShoplyDimens.space2)
    )
    FormConfirmedPassword(
        modifier = modifier.padding(top = ShoplyDimens.space3),
        confirmedPassword = state.form.confirmedPassword,
        showPassword = state.showPassword,
        onConfirmedPasswordChanged = {
            onEvent(SignUpScreenIntent.Change.ConfirmedPassword(it))
        },
        onVisibilityChanged = { onEvent(SignUpScreenIntent.Change.PasswordVisibility(it)) },
        errorMessage = state.userError.passwordConfirmedError?.asString()
    )
}

@Composable
private fun FormUsername(
    modifier: Modifier = Modifier,
    username: String,
    onUsernameChanged: (String) -> Unit,
    errorMessage: String?,
    onClear: () -> Unit
) {
    FormTextField(
        modifier = modifier,
        value = username,
        onValueChange = onUsernameChanged,
        label = stringResource(R.string.username),
        leadingIcon = ShoplyIcons.User,
        trailingIcon = ShoplyIcons.XCircle,
        onTrailingIconClicked = onClear,
        trailingIconContentDescription = stringResource(R.string.clear_username),
        supportingText = errorMessage,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
    )
}

@Composable
private fun FormPhoneNumber(
    modifier: Modifier = Modifier,
    phoneNumber: String,
    onPhoneNumberChanged: (String) -> Unit,
    errorMessage: String?,
    onPhoneNumberCleared: () -> Unit,
    countryLeading: @Composable () -> Unit
) {
    FormTextField(
        modifier = modifier,
        value = phoneNumber,
        onValueChange = onPhoneNumberChanged,
        label = stringResource(R.string.phone_number),
        leadingContent = countryLeading,
        trailingIcon = ShoplyIcons.XCircle,
        onTrailingIconClicked = onPhoneNumberCleared,
        trailingIconContentDescription = stringResource(R.string.clear_phone_number),
        supportingText = errorMessage,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
    )
}

@Composable
private fun FormConfirmedPassword(
    modifier: Modifier = Modifier,
    confirmedPassword: String,
    showPassword: Boolean,
    onConfirmedPasswordChanged: (String) -> Unit,
    errorMessage: String?,
    onVisibilityChanged: (Boolean) -> Unit
) {
    val visibilityIcon = ImageVector.vectorResource(
        id = if (showPassword) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off
    )

    FormTextField(
        modifier = modifier,
        value = confirmedPassword,
        onValueChange = onConfirmedPasswordChanged,
        label = stringResource(R.string.confirmed_password),
        leadingIcon = ShoplyIcons.Lock,
        trailingIcon = visibilityIcon,
        onTrailingIconClicked = { onVisibilityChanged(showPassword) },
        trailingIconContentDescription = stringResource(
            if (showPassword) R.string.visibility_on else R.string.visibility_off
        ),
        supportingText = errorMessage,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
    )
}

@Composable
private fun SignUpAlternativesRow(
    onFacebookSignUpClicked: () -> Unit,
    onGoogleSignUpClicked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space3)
    ) {
        FacebookButton(
            modifier = Modifier.weight(1f),
            compact = true,
            onClick = onFacebookSignUpClicked
        )
        GoogleButton(
            modifier = Modifier.weight(1f),
            compact = true,
            onClick = onGoogleSignUpClicked
        )
    }
}

@Composable
fun AlreadyHaveAccountRow(
    onSignInClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ShoplyDimens.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.already_have_account),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.width(ShoplyDimens.space1))
        ShoplyButton(
            label = stringResource(R.string.sign_in),
            variant = ShoplyButtonVariant.Ghost,
            onClick = onSignInClicked,
        )
    }
}

@Preview(
    name = "Signup Screen",
    showSystemUi = true,
    uiMode = UiModeManager.MODE_NIGHT_YES
)
@Composable
private fun SignupContentPreview() {
    val state = SignUpScreenState()
    ShoplyTheme {
        SignUpContent(
            isExpandedScreen = false,
            state = state,
            onEvent = {}
        )
    }
}
