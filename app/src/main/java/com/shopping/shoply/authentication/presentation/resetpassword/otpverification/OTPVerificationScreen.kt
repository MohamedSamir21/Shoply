package com.shopping.shoply.authentication.presentation.resetpassword.otpverification

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.OtpInput
import com.shopping.shoply.authentication.presentation.components.SendAgainComponent
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.components.ShoplyTopBar
import com.shopping.shoply.common.presentation.components.LoadingLogoDialog
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.util.maskPhoneNumberKeepingLast

private const val TAG = "OTPVerificationScreen"

@Composable
fun OTPVerificationScreen(
    navController: NavHostController,
    expandedScreen: Boolean
) {
    val viewModel = hiltViewModel<OTPVerificationViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val otpCodeVerificationResult = viewModel.otpCodeVerificationResult

    LaunchedEffect(otpCodeVerificationResult) {
        otpCodeVerificationResult.collect { result ->
            when (result) {
                OTPVerificationEvent.Success -> {
                    Log.d(TAG, "Success!!")
                    when (viewModel.purpose) {
                        OtpPurpose.RESET -> {
                            navController.navigate(NavRoutes.ResetPassword.route) {
                                popUpTo(NavRoutes.SignIn.route) { inclusive = false }
                            }
                        }

                        OtpPurpose.SIGNUP_LINK -> {
                            navController.navigate(NavRoutes.VerifyAccount.route) {
                                popUpTo(NavRoutes.SignUp.route) { inclusive = true }
                            }
                        }
                    }
                }

                is OTPVerificationEvent.Error -> {
                    Log.d(TAG, "Error!!")
                    Toast.makeText(
                        context,
                        result.message.asString(context),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    OTPVerificationContent(
        expandedScreen = expandedScreen,
        state = state,
        // Skipping is only offered during sign-up; password reset must complete the OTP.
        showSkip = viewModel.purpose == OtpPurpose.SIGNUP_LINK,
        onCodeChange = { viewModel.onIntent(OTPVerificationIntent.ChangeCode(it)) },
        onBack = { navController.popBackStack() },
        onResendCodeAction = { viewModel.onIntent(OTPVerificationIntent.Resend) },
        onConfirm = viewModel::proceedToVerifyOTPCode,
        onSkip = {
            navController.navigate(NavRoutes.VerifyAccount.route) {
                popUpTo(NavRoutes.SignUp.route) { inclusive = true }
            }
        }
    )
}

@Composable
internal fun OTPVerificationContent(
    modifier: Modifier = Modifier,
    expandedScreen: Boolean,
    state: OTPVerificationState,
    showSkip: Boolean = false,
    onBack: () -> Unit,
    onCodeChange: (String) -> Unit,
    onResendCodeAction: () -> Unit,
    onConfirm: () -> Unit,
    onSkip: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = ShoplyDimens.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShoplyTopBar(
            title = stringResource(R.string.verification_code),
            onBack = onBack,
        )

        // Message + slots + resend sit centred in the space between the top bar and
        // the bottom CTA, mirroring the handoff OTP screen.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(stringResource(R.string.otp_verifiatoin_code_sending_guide_message))
                        append(" ")
                    }
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                    ) {
                        append(
                            maskPhoneNumberKeepingLast(
                                countryCode = state.countryCode,
                                phoneNumber = state.phoneNumber
                            )
                        )
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(ShoplyDimens.space8))
            OtpInput(
                value = state.enteredCode,
                onValueChange = onCodeChange,
                length = OTP_CODE_LENGTH,
            )
            Spacer(Modifier.height(ShoplyDimens.space6))
            SendAgainComponent(
                sendText = stringResource(R.string.resend_code_in),
                count = state.countDownTime,
                countDownFinished = state.countDownFinished,
                resendLabel = stringResource(R.string.resend_code),
                onResendAction = onResendCodeAction,
                showResendButtonWhileCountingDown = false,
                symbolAroundCount = Pair("", ""),
            )
        }

        if (state.isLoading) {
            LoadingLogoDialog { }
        } else {
            ShoplyButton(
                modifier = if (expandedScreen) Modifier else Modifier.fillMaxWidth(),
                label = stringResource(R.string.confirm),
                onClick = onConfirm
            )
        }
        if (showSkip) {
            TextButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onSkip
            ) {
                Text(
                    text = stringResource(R.string.skip_for_now),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(ShoplyDimens.space5))
    }
}

@PreviewLightDark
@Composable
private fun OTPVerificationContentPreview() {
    ShoplyTheme {
        OTPVerificationContent(
            expandedScreen = false,
            state = OTPVerificationState(enteredCode = "123", phoneNumber = "15551234590"),
            onCodeChange = {},
            onBack = {},
            onResendCodeAction = { },
            onConfirm = {}
        )
    }
}
