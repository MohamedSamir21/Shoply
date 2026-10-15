package com.shopping.shoply.authentication.presentation.verifyaccount

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.SendEmailLinkComponent
import com.shopping.shoply.authentication.presentation.components.ShoplyTopBar
import com.shopping.shoply.authentication.presentation.result.ResultPurpose
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun VerifyAccountScreen(
    navController: NavController
) {
    val viewModel: VerifyAccountViewModel = hiltViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val email = state.value.email
    val countDownTime = state.value.countDownTime
    val countDownFinished = state.value.countDownFinished
    val sendEmailLinkSuccess = state.value.sendEmailLinkSuccessMessage
    val sendEmailLinkError = state.value.sendEmailLinkError
    val isEmailVerified = state.value.isEmailVerified
    val verificationError = state.value.verificationError
    val context = LocalContext.current

    // Re-check verification whenever the screen resumes (e.g. user returns from the
    // browser after clicking the link). Silent: no toast if still unverified.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshVerificationSilently()
    }

    // A failed "Continue to home" (email not yet verified) lands on the standalone
    // Error result screen, matching the design's "Cannot verify you" terminal.
    LaunchedEffect(verificationError) {
        verificationError?.let {
            viewModel.resetVerificationError()
            navController.navigate(NavRoutes.Result.build(ResultPurpose.VERIFY_FAILED.name))
        }
    }

    LaunchedEffect(sendEmailLinkSuccess) {
        sendEmailLinkSuccess?.let {
            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
            viewModel.resetEmailLinkSuccessMessage()
        }
    }
    LaunchedEffect(sendEmailLinkError) {
        sendEmailLinkError?.let {
            Toast.makeText(context, it.asString(context), Toast.LENGTH_LONG).show()
            viewModel.resetEmailLinkErrorMessage()
        }
    }

    // On verification success, advance to the standalone Success result screen.
    LaunchedEffect(isEmailVerified) {
        if (isEmailVerified) {
            navController.navigate(NavRoutes.Result.build(ResultPurpose.EMAIL_VERIFIED.name))
        }
    }

    VerifyAccountContent(
        email = email,
        count = countDownTime,
        countDownFinished = countDownFinished,
        onResend = { viewModel.onIntent(VerifyAccountIntent.Resend) },
        onForwardAction = { viewModel.onIntent(VerifyAccountIntent.ConfirmVerified) }
    )
}

@Composable
internal fun VerifyAccountContent(
    modifier: Modifier = Modifier,
    email: String?,
    count: String,
    countDownFinished: Boolean,
    onResend: () -> Unit,
    onForwardAction: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ShoplyTopBar(title = stringResource(R.string.verify_your_account))
        SendEmailLinkComponent(
            message = stringResource(R.string.check_verification_link_sent_to),
            email = email,
            forwardButtonTitle = stringResource(R.string.continue_to_home),
            count = count,
            countDownFinished = countDownFinished,
            sendAgainText = stringResource(R.string.send_verification_link_again_in),
            onResendAction = onResend,
            onForwardAction = onForwardAction,
            verticallyCentered = true,
            pulse = true,
        )
    }
}

@PreviewLightDark
@Composable
private fun VerifyAccountContentPreview() {
    ShoplyTheme {
        VerifyAccountContent(
            email = "username@gmail.com",
            count = "00:40",
            countDownFinished = true,
            onResend = {},
            onForwardAction = {}
        )
    }
}
