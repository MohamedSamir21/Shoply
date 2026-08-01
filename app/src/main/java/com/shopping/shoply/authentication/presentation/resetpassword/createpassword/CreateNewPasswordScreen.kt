package com.shopping.shoply.authentication.presentation.resetpassword.createpassword

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.FormPassword
import com.shopping.shoply.authentication.presentation.components.FormTextField
import com.shopping.shoply.authentication.presentation.components.PasswordStrengthMeter
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.components.ShoplyTopBar
import com.shopping.shoply.authentication.presentation.result.ResultPurpose
import com.shopping.shoply.common.presentation.components.LoadingLogoDialog
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun CreateNewPasswordScreen(
    navController: NavController,
    isExpandedScreen: Boolean
) {
    val viewModel: CreateNewPasswordViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
            viewModel.resetError()
        }
    }

    // On success, advance to the standalone Success result screen (which sends the
    // user on to Sign In to log in with the new password).
    LaunchedEffect(state.resetSucceeded) {
        if (state.resetSucceeded) {
            navController.navigate(NavRoutes.Result.build(ResultPurpose.PASSWORD_RESET.name))
        }
    }

    CreateNewPasswordContent(
        isExpandedScreen = isExpandedScreen,
        state = state,
        onIntent = viewModel::onIntent,
        onBack = { navController.popBackStack() },
    )
}

@Composable
private fun CreateNewPasswordContent(
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean,
    state: CreateNewPasswordState,
    onIntent: (CreateNewPasswordIntent) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = ShoplyDimens.gutter)
    ) {
        ShoplyTopBar(
            title = stringResource(R.string.create_new_password),
            onBack = onBack,
        )
        Spacer(Modifier.height(ShoplyDimens.space2))
        Text(
            text = stringResource(R.string.create_new_password_guiding_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(ShoplyDimens.space6))
        FormPassword(
            password = state.password,
            label = stringResource(R.string.new_password),
            showPassword = state.showPassword,
            onPasswordChanged = { onIntent(CreateNewPasswordIntent.ChangePassword(it)) },
            onVisibilityChanged = { onIntent(CreateNewPasswordIntent.ChangePasswordVisibility(it)) },
            errorMessage = state.passwordError?.asString()
        )
        Spacer(Modifier.height(ShoplyDimens.space2))
        PasswordStrengthMeter(strength = state.passwordStrength)
        Spacer(Modifier.height(ShoplyDimens.space3))
        ConfirmedPasswordField(
            confirmedPassword = state.confirmedPassword,
            showPassword = state.showPassword,
            onConfirmedPasswordChanged = {
                onIntent(CreateNewPasswordIntent.ChangeConfirmedPassword(it))
            },
            onVisibilityChanged = { onIntent(CreateNewPasswordIntent.ChangePasswordVisibility(it)) },
            errorMessage = state.confirmedPasswordError?.asString()
        )

        Spacer(Modifier.weight(1f))

        if (state.isProcessing) {
            LoadingLogoDialog { }
        } else {
            ShoplyButton(
                modifier = if (isExpandedScreen) Modifier else Modifier.fillMaxWidth(),
                label = stringResource(R.string.reset_password_button),
                onClick = { onIntent(CreateNewPasswordIntent.Submit) }
            )
        }
        Spacer(Modifier.height(ShoplyDimens.space5))
    }
}

@Composable
private fun ConfirmedPasswordField(
    modifier: Modifier = Modifier,
    confirmedPassword: String,
    showPassword: Boolean,
    onConfirmedPasswordChanged: (String) -> Unit,
    onVisibilityChanged: (Boolean) -> Unit,
    errorMessage: String?
) {
    val visibilityIcon = ImageVector.vectorResource(
        id = if (showPassword) R.drawable.ic_visibility_on else R.drawable.ic_visibility_off
    )

    FormTextField(
        modifier = modifier,
        value = confirmedPassword,
        onValueChange = { onConfirmedPasswordChanged(it) },
        onTrailingIconClicked = { onVisibilityChanged(showPassword) },
        label = stringResource(R.string.confirm_new_password),
        leadingIcon = ShoplyIcons.Lock,
        trailingIcon = visibilityIcon,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        visualTransformation = if (showPassword) VisualTransformation.None
        else PasswordVisualTransformation(),
        supportingText = errorMessage
    )
}

@PreviewLightDark
@Composable
private fun CreateNewPasswordContentPreview() {
    ShoplyTheme {
        CreateNewPasswordContent(
            isExpandedScreen = false,
            state = CreateNewPasswordState(),
            onIntent = {},
            onBack = {},
        )
    }
}
