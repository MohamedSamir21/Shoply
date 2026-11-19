package com.shopping.shoply.authentication.presentation.signin

import android.content.res.Configuration
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
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
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.components.ShoplyButtonVariant
import com.shopping.shoply.common.presentation.components.ShoplyLogo
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.navigation.navigateToHome
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import kotlinx.coroutines.flow.collectLatest

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
                    navController.navigateToHome()
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
        onChange = { viewModel.onIntent(it) },
        onForgotPasswordClicked = {
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
    onSignUpClicked: () -> Unit,
    // A slot so screen tests can swap in plain buttons: the real Facebook button needs Hilt and
    // the Facebook SDK, which a JVM UI test doesn't have.
    socialButtons: @Composable (onFacebookClick: () -> Unit, onGoogleClick: () -> Unit) -> Unit =
        { onFacebookClick, onGoogleClick -> SignInSocialButtons(onFacebookClick, onGoogleClick) }
) {
    val configuration = LocalConfiguration.current
    val isInLandscapeMode = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val signInError = state.signInError
    val emailHasError = state.emailValidationError != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ShoplyDimens.gutter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(ShoplyDimens.space4))
        ShoplyLogo(size = 66.dp)
        Spacer(Modifier.height(ShoplyDimens.space5))
        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(ShoplyDimens.space2))
        Text(
            text = stringResource(R.string.login_guiding),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(ShoplyDimens.space6))
        socialButtons(
            { onChange(SignInScreenIntent.ContinueWithFacebook) },
            { onChange(SignInScreenIntent.ContinueWithGoogle) }
        )

        if (!isInLandscapeMode) {
            Spacer(Modifier.height(ShoplyDimens.space5))
            ContinueWithRow()
            Spacer(Modifier.height(ShoplyDimens.space4))
            FormEmail(
                modifier = Modifier.fillMaxWidth(),
                email = state.email,
                showSupportingText = emailHasError,
                errorMessage = state.emailValidationError?.asString(),
                onEmailChanged = { onChange(SignInScreenIntent.Change.Email(it)) },
                onClear = { onChange(SignInScreenIntent.Clear.Email) }
            )
            FormPassword(
                modifier = Modifier.fillMaxWidth(),
                password = state.password,
                showSupportingText = false,
                errorMessage = null,
                onPasswordChanged = { onChange(SignInScreenIntent.Change.Password(it)) },
                onVisibilityChanged = { onChange(SignInScreenIntent.Change.PasswordVisibility) },
                showPassword = state.showPassword
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                ShoplyButton(
                    label = stringResource(R.string.forgot_password).plus("?"),
                    variant = ShoplyButtonVariant.Ghost,
                    onClick = onForgotPasswordClicked,
                )
            }
            signInError?.let {
                Spacer(Modifier.height(ShoplyDimens.space2))
                Text(
                    text = it.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(ShoplyDimens.space4))
            if (state.isSigningIn) {
                CircularProgressIndicator()
            } else {
                ShoplyButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.sign_in),
                    onClick = { onChange(SignInScreenIntent.SignIn) },
                    enable = state.enableSignIn
                )
            }
            RegisterRow(onSignUpClicked = onSignUpClicked)
            Spacer(Modifier.height(ShoplyDimens.space5))
        }
    }
}

@Composable
private fun SignInSocialButtons(
    onFacebookClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FacebookButton(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.continue_with_facebook),
            onClick = onFacebookClick
        )
        Spacer(Modifier.height(ShoplyDimens.space3))
        GoogleButton(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.continue_with_google),
            onClick = onGoogleClick
        )
    }
}

@Composable
private fun RegisterRow(
    onSignUpClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = ShoplyDimens.space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.didn_no_have_account),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.width(ShoplyDimens.space1))
        ShoplyButton(
            label = stringResource(R.string.register),
            variant = ShoplyButtonVariant.Ghost,
            onClick = onSignUpClicked,
        )
    }
}

@PreviewLightDark
@Composable
private fun SignInContentPreview() {
    val state = SignInScreenState(email = "", password = "")
    ShoplyTheme {
        SignInContent(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            state = state,
            isExpandedScreen = false,
            onChange = {},
            onForgotPasswordClicked = {},
            onSignUpClicked = {}
        )
    }
}
