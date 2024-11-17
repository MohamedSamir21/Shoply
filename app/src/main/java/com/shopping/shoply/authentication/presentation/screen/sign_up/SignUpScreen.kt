package com.shopping.shoply.authentication.presentation.screen.sign_up

import android.app.UiModeManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.viewmodel.SignUpViewModel
import com.shopping.shoply.common.presentation.navigation.NavRoutes
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
        viewModel.signUpResult.collectLatest {
            when (it) {
                is SignUpScreenEvent.SignUp.Result.Success -> {
                    navHostController.navigate(NavRoutes.VerifyAccount.value)
                }

                is SignUpScreenEvent.SignUp.Result.Failure -> {
                    viewModel.onEvent(SignUpScreenEvent.SignUp.Result.Failure(it.uiText))
                }
            }
        }
    }

    SignUpContent(
        isExpandedScreen = isExpandedScreen,
        state = state.value,
        onEvent = {
            viewModel.onEvent(it)
        }
    )
}

@Composable
private fun SignUpContent(
    isExpandedScreen: Boolean,
    state: SignUpScreenState,
    onEvent: (SignUpScreenEvent) -> Unit
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BackgroundImage(Modifier.align(Alignment.TopEnd))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CreateAccountText()
            Spacer(modifier = Modifier.height(10.dp))
            PromoText()
            Spacer(modifier = Modifier.height(20.dp))
            Form(state, isExpandedScreen, onEvent)
            if (isExpandedScreen.not()) {
                ContinueWithRow()
                SignUpAlternativesRow()
                AlreadyHaveAccountRow(
                    onSignInClicked = {}
                )
            }
        }
    }
}

@Composable
private fun BackgroundImage(
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier
            .alpha(0.1f)
            .size(200.dp),
        painter = painterResource(id = R.drawable.online_shopping_illustration),
        contentDescription = "",
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun CreateAccountText() {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        text = stringResource(R.string.create_account),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
private fun PromoText() {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.promo_text),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun Form(
    state: SignUpScreenState,
    isExpandedScreen: Boolean,
    onEvent: (SignUpScreenEvent) -> Unit
) {
    val focusRequester = remember {
        FocusRequester()
    }
    val signUpError = state.signUpError?.asString()

    FormTextFields(
        modifier = if (isExpandedScreen)
            Modifier.wrapContentWidth()
        else Modifier
            .fillMaxWidth(),
        state = state,
        onEvent = { onEvent(it) }
    )
    signUpError?.let {
        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
    Spacer(modifier = Modifier.height(15.dp))
    if (state.isSigningUp) {
        CircularProgressIndicator()
    } else {
        SignUpButton(
            modifier = if (isExpandedScreen)
                Modifier
                    .wrapContentWidth()
                    .focusRequester(focusRequester)
            else Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .focusRequester(focusRequester),
            onClick = {
                focusRequester.requestFocus()
                onEvent(SignUpScreenEvent.SignUp.Submit)
            },
            enable = state.enableSignUp
        )
    }
}

@Composable
private fun FormTextFields(
    modifier: Modifier = Modifier,
    state: SignUpScreenState,
    onEvent: (SignUpScreenEvent) -> Unit
) {
    FormUsername(
        modifier = modifier,
        username = state.user.username,
        onUsernameChanged = { onEvent(SignUpScreenEvent.Change.Username(it)) },
        errorMessage = state.userError.usernameError,
        onClear = { onEvent(SignUpScreenEvent.Clear.Username) }
    )
    FormEmail(
        modifier = modifier.padding(top = 10.dp),
        email = state.user.email,
        onEmailChanged = { onEvent(SignUpScreenEvent.Change.Email(it)) },
        errorMessage = state.userError.emailError,
        onClear = { onEvent(SignUpScreenEvent.Clear.Email) }
    )
    FormPhoneNumber(
        modifier = modifier.padding(top = 10.dp),
        phoneNumber = state.user.phoneNumber,
        onPhoneNumberChanged = { onEvent(SignUpScreenEvent.Change.PhoneNumber(it)) },
        onPhoneNumberCleared = { onEvent(SignUpScreenEvent.Clear.PhoneNumber) },
        errorMessage = state.userError.phoneNumberError,
    )
    FormPassword(
        modifier = modifier.padding(top = 10.dp),
        password = state.user.password,
        onPasswordChanged = { onEvent(SignUpScreenEvent.Change.Password(it)) },
        onVisibilityChanged = { onEvent(SignUpScreenEvent.Change.PasswordVisibility(it)) },
        showPassword = state.showPassword,
        errorMessage = state.userError.passwordError,
    )
    FormConfirmedPassword(
        modifier = modifier.padding(top = 10.dp),
        confirmedPassword = state.user.confirmedPassword,
        showPassword = state.showPassword,
        onConfirmedPasswordChanged = {
            onEvent(
                SignUpScreenEvent.Change.ConfirmedPassword(
                    it
                )
            )
        },
        onVisibilityChanged = { onEvent(SignUpScreenEvent.Change.PasswordVisibility(it)) },
        errorMessage = state.userError.passwordConfirmedError
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
        value = username,
        onValueChange = { onUsernameChanged(it) },
        onTrailingIconClicked = onClear,
        label = stringResource(R.string.username),
        trailingIcon = Icons.Outlined.Clear,
        leadingIcon = Icons.Outlined.Person,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        supportingText = errorMessage,
        modifier = modifier
    )
}

@Composable
private fun FormEmail(
    modifier: Modifier = Modifier,
    email: String,
    onEmailChanged: (String) -> Unit,
    errorMessage: String?,
    onClear: () -> Unit
) {
    FormTextField(
        value = email,
        onValueChange = { onEmailChanged(it) },
        onTrailingIconClicked = onClear,
        label = stringResource(R.string.email),
        leadingIcon = ImageVector.vectorResource(id = R.drawable.ic_email),
        trailingIcon = Icons.Outlined.Clear,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        supportingText = errorMessage,
        modifier = modifier
    )
}

@Composable
private fun FormPhoneNumber(
    modifier: Modifier = Modifier,
    phoneNumber: String,
    onPhoneNumberChanged: (String) -> Unit,
    errorMessage: String?,
    onPhoneNumberCleared: () -> Unit
) {
    FormTextField(
        value = phoneNumber,
        onValueChange = { onPhoneNumberChanged(it) },
        onTrailingIconClicked = onPhoneNumberCleared,
        label = stringResource(R.string.phone_number),
        leadingIcon = Icons.Outlined.Phone,
        trailingIcon = Icons.Outlined.Clear,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        supportingText = errorMessage,
        modifier = modifier
    )
}

@Composable
private fun FormPassword(
    modifier: Modifier = Modifier,
    password: String,
    showPassword: Boolean,
    onPasswordChanged: (String) -> Unit,
    errorMessage: String?,
    onVisibilityChanged: (Boolean) -> Unit
) {
    val visibilityIcon = ImageVector.vectorResource(
        id = if (showPassword) {
            R.drawable.ic_visibility_on
        } else {
            R.drawable.ic_visibility_off
        }
    )

    FormTextField(
        value = password,
        onValueChange = { onPasswordChanged(it) },
        onTrailingIconClicked = { onVisibilityChanged(showPassword.not()) },
        label = stringResource(R.string.password),
        leadingIcon = Icons.Outlined.Lock,
        trailingIcon = visibilityIcon,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        supportingText = errorMessage,
        modifier = modifier
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
        id = if (showPassword) {
            R.drawable.ic_visibility_on
        } else {
            R.drawable.ic_visibility_off
        }
    )

    FormTextField(
        value = confirmedPassword,
        onValueChange = { onConfirmedPasswordChanged(it) },
        onTrailingIconClicked = { onVisibilityChanged(showPassword.not()) },
        label = stringResource(R.string.confirmed_password),
        leadingIcon = Icons.Outlined.Lock,
        trailingIcon = visibilityIcon,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        supportingText = errorMessage,
        modifier = modifier
    )
}

@Composable
private fun FormTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector,
    onTrailingIconClicked: () -> Unit,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    var hasFocus by remember {
        mutableStateOf(false)
    }
    val focusColor = if (hasFocus)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.tertiary

    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        label = {
            Text(text = label)
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "",
                tint = focusColor
            )
        },
        trailingIcon = {
            if (hasFocus) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = "",
                    modifier = Modifier.clickable { onTrailingIconClicked() },
                    tint = focusColor
                )
            }
        },
        supportingText = {
            Text(
                text = if (supportingText.isNullOrEmpty())
                    "*${stringResource(R.string.required)}"
                else supportingText,
                color = if (supportingText != null)
                    MaterialTheme.colorScheme.error
                else focusColor
            )
        },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        modifier = modifier.onFocusChanged {
            hasFocus = it.isFocused
        }
    )
}

@Composable
fun SignUpButton(
    modifier: Modifier = Modifier,
    enable: Boolean,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        enabled = enable,
        onClick = onClick
    ) {
        Text(text = stringResource(R.string.sign_up))
    }
}

@Composable
fun ContinueWithRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HorizontalDivider(modifier = Modifier.width(100.dp))
        Text(
            text = stringResource(R.string.or_continue_with),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.width(100.dp))
    }
}

@Composable
private fun SignUpAlternativesRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FacebookButton(
            modifier = Modifier.weight(1f),
            onClick = { }
        )
        GoogleButton(
            modifier = Modifier.weight(1f),
            onClick = { }
        )
    }
}

@Composable
fun GoogleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_google),
            contentDescription = stringResource(
                R.string.google
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(id = R.string.google)
        )
    }
}

@Composable
fun FacebookButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_facebook),
            contentDescription = stringResource(R.string.facebook)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(id = R.string.facebook)
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
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.already_have_account),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(10.dp))
        TextButton(onClick = onSignInClicked) {
            Text(text = stringResource(R.string.sign_in))
        }
    }
}

@Preview(
    name = "Signup Screen",
    showSystemUi = true,
    uiMode = UiModeManager.MODE_NIGHT_YES,
    device = Devices.TABLET
)
@Composable
private fun SignupContentPreview() {
    val state = SignUpScreenState()
    ShoplyTheme {
        SignUpContent(
            isExpandedScreen = true,
            state = state,
            onEvent = {

            }
        )
    }
}
