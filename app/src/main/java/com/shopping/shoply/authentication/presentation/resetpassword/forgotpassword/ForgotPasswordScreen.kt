package com.shopping.shoply.authentication.presentation.resetpassword.forgotpassword

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.MethodCard
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.components.ShoplyTopBar
import com.shopping.shoply.authentication.presentation.resetpassword.forgotpassword.model.ResetMethod
import com.shopping.shoply.authentication.presentation.resetpassword.otpverification.OtpPurpose
import com.shopping.shoply.authentication.presentation.utility.getResetMethods
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

private const val TAG = "ForgotPasswordScreen"

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    isExpandedScreen: Boolean
) {
    val viewModel: ForgotPasswordViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ForgotPasswordContent(
        isExpandedScreen = isExpandedScreen,
        resetMethods = state.resetMethods,
        selectedResetMethodIndex = state.selectedResetMethodIndex,
        onResetMethodChecked = viewModel::onSelectResetMethod,
        onNext = {
            if (state.selectedResetMethodIndex == 0) {
                onEmailChoice(state.email, navController)
            } else if (state.selectedResetMethodIndex == 1) {
                onPhoneChoice(state.phoneNumber, state.isPhoneVerified, navController)
            }
        },
        onBack = { navController.popBackStack() }
    )
}

private fun onPhoneChoice(
    phoneNumber: String?,
    isPhoneVerified: Boolean,
    navController: NavController
) {
    if (isPhoneVerified && !phoneNumber.isNullOrEmpty() && phoneNumber.trim().startsWith("+")) {
        val e164Digits = phoneNumber.filter(Char::isDigit)
        navController.navigate(
            NavRoutes.OTPCodeVerification.route
                .replace(
                    oldValue = "{${NavRoutes.OTPCodeVerification.Args.PHONE_NUMBER_WITH_COUNTRY_CODE}}",
                    newValue = e164Digits
                )
                .replace(
                    oldValue = "{${NavRoutes.OTPCodeVerification.Args.PURPOSE}}",
                    newValue = OtpPurpose.RESET.name
                )
        )
        Log.d(TAG, e164Digits)
    } else {
        navController.navigate(NavRoutes.EnterPhone.route)
    }
}

private fun onEmailChoice(
    email: String?,
    navController: NavController
) {
    if (!email.isNullOrEmpty()) {
        navController.navigate(
            NavRoutes.SendResetLink.route.replace(oldValue = "{email}", newValue = email)
        )
    } else {
        navController.navigate(NavRoutes.EnterEmail.route)
    }
}

@Composable
internal fun ForgotPasswordContent(
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean,
    resetMethods: List<ResetMethod>,
    selectedResetMethodIndex: Int,
    onResetMethodChecked: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = ShoplyDimens.gutter)
    ) {
        ShoplyTopBar(
            title = stringResource(R.string.forgot_password),
            onBack = onBack,
        )
        Spacer(Modifier.height(ShoplyDimens.space2))
        Text(
            text = stringResource(R.string.forgot_password_guiding_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(ShoplyDimens.space6))

        resetMethods.forEachIndexed { index, restMethod ->
            val icon = if (index == 0) ShoplyIcons.Mail else ShoplyIcons.Phone
            MethodCard(
                icon = icon,
                kind = restMethod.name.asString(),
                value = restMethod.message ?: restMethod.name.asString(),
                selected = index == selectedResetMethodIndex,
                onClick = onResetMethodChecked,
            )
            Spacer(Modifier.height(ShoplyDimens.space3))
        }

        Spacer(Modifier.weight(1f))
        ShoplyButton(
            modifier = if (!isExpandedScreen) Modifier.fillMaxWidth() else Modifier,
            label = stringResource(R.string.next),
            onClick = onNext
        )
        Spacer(Modifier.height(ShoplyDimens.space5))
    }
}

@PreviewLightDark
@Composable
private fun ForgotPasswordContentPreview() {
    ShoplyTheme {
        ForgotPasswordContent(
            isExpandedScreen = false,
            resetMethods = getResetMethods(),
            selectedResetMethodIndex = 0,
            onResetMethodChecked = {},
            onNext = {},
            onBack = {}
        )
    }
}
