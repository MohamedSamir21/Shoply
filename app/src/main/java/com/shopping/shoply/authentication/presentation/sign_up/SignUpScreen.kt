package com.shopping.shoply.authentication.presentation.sign_up

import android.app.UiModeManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ContinueWithRow
import com.shopping.shoply.authentication.presentation.components.FacebookButton
import com.shopping.shoply.authentication.presentation.components.FormEmail
import com.shopping.shoply.authentication.presentation.components.FormPassword
import com.shopping.shoply.authentication.presentation.components.FormTextField
import com.shopping.shoply.authentication.presentation.components.GoogleButton
import com.shopping.shoply.authentication.presentation.components.RoundedShoplyButton
import com.shopping.shoply.authentication.presentation.sign_up.model.ActivityContext
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "SignUpScreen"

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
                        SignUpScreenEvent.SignUpResult.Success.NotVerified -> { // In case the user uses sign up form, then we need to send him verification email!!
                            navHostController.popBackStack()
                            navHostController.navigate(
                                NavRoutes.VerifyAccount.route.replace(
                                    "{email}",
                                    state.value.user.email
                                )
                            )
                        }

                        SignUpScreenEvent.SignUpResult.Success.Verified -> { // In case the user uses Facebook or Google Sign up, he is verified by default!!
                            navHostController.popBackStack(
                                route = NavRoutes.SignUp.route,
                                inclusive = true
                            )
                            navHostController.navigate(
                                NavRoutes.Home.route
                            )
                        }
                    }
                }

                is SignUpScreenEvent.SignUpResult.Failure -> {
                    viewModel.onIntent(SignUpScreenEvent.SignUpResult.Failure(result.uiText))
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
private fun SignUpContent(
    isExpandedScreen: Boolean,
    state: SignUpScreenState,
    onEvent: (SignUpScreenIntent) -> Unit
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
                SignUpAlternativesRow(
                    onGoogleSignUpClicked = { onEvent(SignUpScreenIntent.SignUp.Google(it)) },
                    onFacebookSignUpClicked = {
                        onEvent(
                            SignUpScreenIntent.SignUp.Facebook(
                                it.loginManager,
                                it.callbackManager
                            )
                        )
                    }
                )
                AlreadyHaveAccountRow(
                    onSignInClicked = {
                        onEvent(SignUpScreenIntent.SignIn)
                    }
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
    onEvent: (SignUpScreenIntent) -> Unit
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
        RoundedShoplyButton(
            label = stringResource(R.string.sign_up),
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
                onEvent(SignUpScreenIntent.SignUp.SubmitForm)
            },
            enable = state.enableSignUp
        )
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
        username = state.user.username,
        onUsernameChanged = { onEvent(SignUpScreenIntent.Change.Username(it)) },
        errorMessage = state.userError.usernameError,
        onClear = { onEvent(SignUpScreenIntent.Clear.Username) }
    )
    FormEmail(
        modifier = modifier.padding(top = 10.dp),
        email = state.user.email,
        onEmailChanged = { onEvent(SignUpScreenIntent.Change.Email(it)) },
        errorMessage = state.userError.emailError,
        onClear = { onEvent(SignUpScreenIntent.Clear.Email) }
    )
    FormPhoneNumber(
        modifier = modifier.padding(top = 10.dp),
        phoneNumber = state.user.phoneNumber,
        onPhoneNumberChanged = { onEvent(SignUpScreenIntent.Change.PhoneNumber(it)) },
        onPhoneNumberCleared = { onEvent(SignUpScreenIntent.Clear.PhoneNumber) },
        errorMessage = state.userError.phoneNumberError,
    )
    FormPassword(
        modifier = modifier.padding(top = 10.dp),
        password = state.user.password,
        onPasswordChanged = { onEvent(SignUpScreenIntent.Change.Password(it)) },
        onVisibilityChanged = { onEvent(SignUpScreenIntent.Change.PasswordVisibility(it)) },
        showPassword = state.showPassword,
        errorMessage = state.userError.passwordError,
    )
    FormConfirmedPassword(
        modifier = modifier.padding(top = 10.dp),
        confirmedPassword = state.user.confirmedPassword,
        showPassword = state.showPassword,
        onConfirmedPasswordChanged = {
            onEvent(
                SignUpScreenIntent.Change.ConfirmedPassword(
                    it
                )
            )
        },
        onVisibilityChanged = { onEvent(SignUpScreenIntent.Change.PasswordVisibility(it)) },
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
private fun SignUpAlternativesRow(
    onGoogleSignUpClicked: (ActivityContext) -> Unit,
    onFacebookSignUpClicked: (SignUpScreenIntent.SignUp.Facebook) -> Unit
) {
    val context = LocalContext.current
    val loginManager = remember {
        LoginManager.getInstance()
    }
    val callbackManager = remember {
        CallbackManager.Factory.create()
    }
    val fbLauncher = rememberLauncherForActivityResult(
        loginManager.createLogInActivityResultContract(callbackManager, null)
    ) {}

    DisposableEffect(loginManager) {
        onDispose {
            Log.i(TAG, "onDispose()")
            loginManager.unregisterCallback(callbackManager)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FacebookButton(
            modifier = Modifier.weight(1f),
            textColor = MaterialTheme.colorScheme.inverseSurface,
            onClick = {
                val permissions = listOf("email", "public_profile")
                onFacebookSignUpClicked(
                    SignUpScreenIntent.SignUp.Facebook(
                        loginManager,
                        callbackManager
                    )
                )
                fbLauncher.launch(permissions)
            }
        )
        GoogleButton(
            modifier = Modifier.weight(1f),
            textColor = MaterialTheme.colorScheme.inverseSurface,
            onClick = {
                onGoogleSignUpClicked(ActivityContext(context = context))
            }
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
    uiMode = UiModeManager.MODE_NIGHT_YES
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
