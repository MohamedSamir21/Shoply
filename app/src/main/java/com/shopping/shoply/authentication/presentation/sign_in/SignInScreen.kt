package com.shopping.shoply.authentication.presentation.sign_in

import android.content.res.Configuration
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ContinueWithRow
import com.shopping.shoply.authentication.presentation.components.FacebookButton
import com.shopping.shoply.authentication.presentation.components.FormEmail
import com.shopping.shoply.authentication.presentation.components.FormPassword
import com.shopping.shoply.authentication.presentation.components.GoogleButton
import com.shopping.shoply.authentication.presentation.components.RoundedShoplyButton
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun SignInScreen(
    navController: NavHostController,
    isExpandedScreen: Boolean
) {
    SignInContent(
        state = SignInScreenState("", ""),
        isExpandedScreen = isExpandedScreen,
        onContinueWithFacebook = {},
        onContinueWithGoogle = {},
        onForgotClicked = {},
        onSignInClicked = {},
        onSignUpClicked = {}
    )
}

@Composable
fun SignInContent(
    modifier: Modifier = Modifier,
    state: SignInScreenState,
    isExpandedScreen: Boolean,
    onContinueWithFacebook: () -> Unit,
    onContinueWithGoogle: () -> Unit,
    onForgotClicked: () -> Unit,
    onSignInClicked: () -> Unit,
    onSignUpClicked: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    val isInLandscapeMode = when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> true
        else -> false
    }

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
            onClick = onContinueWithFacebook
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
            onClick = onContinueWithGoogle
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
                showSupportingText = false,
                errorMessage = state.errorMessage,
                onEmailChanged = {},
                onClear = {}
            )
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
                errorMessage = state.errorMessage,
                onPasswordChanged = {},
                onVisibilityChanged = {},
                showPassword = state.showPassword
            )
            Box(
                modifier = if (!isExpandedScreen) {
                    Modifier.fillMaxWidth()
                } else Modifier,
                contentAlignment = Alignment.TopEnd
            ) {
                ForgotPasswordTextButton(
                    onForgotClicked = onForgotClicked
                )
            }
            Spacer(Modifier.height(10.dp))
            RoundedShoplyButton(
                modifier = if (!isExpandedScreen) {
                    Modifier.fillMaxWidth()
                } else Modifier,
                label = stringResource(R.string.sign_in),
                onClick = onSignInClicked
            )
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
            onContinueWithFacebook = {},
            onContinueWithGoogle = {},
            onForgotClicked = {},
            onSignInClicked = {},
            onSignUpClicked = {}
        )
    }
}