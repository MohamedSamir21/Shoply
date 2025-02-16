package com.shopping.shoply.authentication.presentation.sign_in

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ContinueWithRow
import com.shopping.shoply.authentication.presentation.components.FacebookButton
import com.shopping.shoply.authentication.presentation.components.FormEmail
import com.shopping.shoply.authentication.presentation.components.FormPassword
import com.shopping.shoply.authentication.presentation.components.GoogleButton
import com.shopping.shoply.authentication.presentation.components.RoundedShoplyButton
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "SignInScreen"

@Composable
fun SignInScreen(
    navController: NavHostController,
    isExpandedScreen: Boolean
) {
    val viewModel: SignInViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.signInResult) {
        viewModel.signInResult.collectLatest { result ->
            when (result) {
                SignInScreenEvent.SignInResult.Success -> {
                    navController.popBackStack()
                    navController.navigate(NavRoutes.Home.route)
                }

                is SignInScreenEvent.SignInResult.Failure -> {
                    viewModel.onEvent(SignInScreenEvent.SignInResult.Failure(result.uiText))
                }
            }
        }
    }

    SignInContent(
        state = state,
        isExpandedScreen = isExpandedScreen,
        onChange = {
            viewModel.onIntent(it)
        },
        onForgotPasswordClicked = {
            navController.popBackStack()
            navController.navigate(NavRoutes.ForgotPassword.route)
        },
        onSignUpClicked = {
            navController.popBackStack()
            navController.navigate(NavRoutes.SignUp.route)
        }
    )
}

@Composable
fun SignInContent(
    modifier: Modifier = Modifier,
    state: SignInScreenState,
    isExpandedScreen: Boolean,
    onChange: (SignInScreenIntent) -> Unit,
    onForgotPasswordClicked: () -> Unit,
    onSignUpClicked: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    val isInLandscapeMode = when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }
    val signInError = state.signInError
    val emailHasError = state.emailValidationError != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShoplyLogo()
        WelcomeBackText()
        Spacer(Modifier.height(5.dp))
        LoginGuidingText()
        Spacer(Modifier.height(15.dp))
        FacebookButton(
            modifier = if (!isExpandedScreen) {
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp)
            } else {
                Modifier
                    .height(50.dp)
            },
            label = stringResource(R.string.continue_with_facebook),
            textColor = MaterialTheme.colorScheme.inverseSurface,
            onClick = {
                onChange(SignInScreenIntent.ContinueWithFacebook)
            }
        )
        Spacer(Modifier.height(10.dp))
        GoogleButton(
            modifier = if (!isExpandedScreen) {
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp)
            } else {
                Modifier
                    .height(50.dp)
            },
            label = stringResource(R.string.continue_with_google),
            textColor = MaterialTheme.colorScheme.inverseSurface,
            onClick = {
                onChange(SignInScreenIntent.ContinueWithGoogle(context))
            }
        )
        if (!isInLandscapeMode) {

            Spacer(Modifier.height(10.dp))
            ContinueWithRow()
            FormEmail(
                modifier = if (!isExpandedScreen) {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                } else Modifier,
                email = state.email,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                showSupportingText = emailHasError,
                errorMessage = state.emailValidationError,
                onEmailChanged = { onChange(SignInScreenIntent.Change.Email(it)) },
                onClear = { onChange(SignInScreenIntent.Clear.Email) }
            )
            if (emailHasError)
                Spacer(Modifier.height(5.dp))
            FormPassword(
                modifier = if (!isExpandedScreen) {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                } else Modifier,
                password = state.password,
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                showSupportingText = false,
                errorMessage = null,
                onPasswordChanged = { onChange(SignInScreenIntent.Change.Password(it)) },
                onVisibilityChanged = { onChange(SignInScreenIntent.Change.PasswordVisibility(it)) },
                showPassword = state.showPassword
            )
            Box(
                modifier = if (!isExpandedScreen) {
                    Modifier.fillMaxWidth()
                } else Modifier,
                contentAlignment = Alignment.TopEnd
            ) {
                ForgotPasswordTextButton(
                    onForgotClicked = onForgotPasswordClicked
                )
            }
            Spacer(Modifier.height(10.dp))
            signInError?.let {
                Log.d(TAG, it.asString())
                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = it.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            Log.d(TAG, "isSigningIn: ${state.isSigningIn}")
            if (state.isSigningIn) {
                CircularProgressIndicator()
            } else {
                RoundedShoplyButton(
                    modifier = if (!isExpandedScreen) {
                        Modifier.fillMaxWidth()
                    } else Modifier,
                    label = stringResource(R.string.sign_in),
                    onClick = {
                        onChange(SignInScreenIntent.SignIn)
                    },
                    enable = state.enableSignIn
                )
            }
            HaveNotAccountRow(
                onSignUpClicked = onSignUpClicked
            )
        }
    }
}

@Composable
private fun ShoplyLogo() {
    Image(
        imageVector = ImageVector.vectorResource(R.drawable.logo),
        contentDescription = stringResource(R.string.shoply_logo),
        modifier = Modifier.size(150.dp),
        colorFilter = ColorFilter.tint(
            color = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
private fun WelcomeBackText() {
    Text(
        text = stringResource(R.string.welcome_back),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.inverseSurface
    )
}

@Composable
private fun LoginGuidingText() {
    Text(
        text = stringResource(R.string.login_guiding),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Light,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.inverseSurface
    )
}

@Composable
private fun ForgotPasswordTextButton(
    modifier: Modifier = Modifier,
    onForgotClicked: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onForgotClicked
    ) {
        Text(
            text = stringResource(R.string.forgot_password).plus("?")
        )
    }
}

@Composable
fun HaveNotAccountRow(
    onSignUpClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.didn_no_have_account),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(10.dp))
        TextButton(onClick = onSignUpClicked) {
            Text(text = stringResource(R.string.sign_up))
        }
    }
}

@PreviewLightDark
@Composable
private fun SignInContentPreview() {
    val state = SignInScreenState(
        email = "",
        password = ""
    )
    ShoplyTheme {
        SignInContent(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surface
            ),
            state = state,
            isExpandedScreen = false,
            onChange = {},
            onForgotPasswordClicked = {},
            onSignUpClicked = {}
        )
    }
}