package com.shopping.shoply.authentication.presentation.reset_password.enter_email

import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.EnterMethodComponent
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EnterEmailScreen(
    navController: NavController,
    isExpandedScreen: Boolean
) {
    val viewModel: EnterEmailViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.emailVerificationResult) {
        viewModel.emailVerificationResult.collectLatest {
            navController.popBackStack()
            navController.navigate(NavRoutes.SendRestLink.route
                .replace(
                    "{email}",
                    state.email
                )
            )
        }
    }

    EnterMethodComponent(
        isExpandedScreen = isExpandedScreen,
        methodName = stringResource(R.string.email),
        methodMessage = stringResource(R.string.enter_email_guiding_message),
        methodValue = state.email,
        isVerifyingValue = state.isProcessing,
        errorMessage = state.error,
        inputLabel = "username@domain.com",
        onMethodValueChange = {
            viewModel.onChangeEmail(it)
        },
        onBack = {
            navController.popBackStack()
        },
        onNext = viewModel::onNext,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Go
        )
    )
}

@PreviewLightDark
@Composable
private fun EnterMethodComponentContentPreview() {
    ShoplyTheme {
        EnterMethodComponent(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            isExpandedScreen = false,
            methodName = stringResource(R.string.email),
            methodMessage = stringResource(R.string.enter_email_guiding_message),
            methodValue = "",
            isVerifyingValue = false,
            inputLabel = "username@domain.com",
            onMethodValueChange = {},
            onBack = {},
            onNext = {}
        )
    }
}

