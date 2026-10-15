package com.shopping.shoply.authentication.presentation.resetpassword.sendresetlink

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.SendEmailLinkComponent
import com.shopping.shoply.authentication.presentation.components.ShoplyTopBar
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun SendResetLinkScreen(
    navController: NavController,
    email: String
) {
    val viewModel: SendResetLinkViewModel = hiltViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val countDownTime = state.value.countDownTime
    val countDownFinished = state.value.countDownFinished
    val sendEmailLinkSuccess = state.value.sendEmailLinkSuccessMessage
    val sendEmailLinkError = state.value.sendEmailLinkError
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(SendResetLinkIntent.OnFirstPopUp(email))
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

    SendResetLinkContent(
        email = email,
        count = countDownTime,
        countDownFinished = countDownFinished,
        onBack = { navController.popBackStack() },
        onResend = { viewModel.onIntent(SendResetLinkIntent.Resend) },
        onForwardAction = {
            navController.popBackStack()
            navController.navigate(NavRoutes.SignIn.route)
        }
    )
}

@Composable
internal fun SendResetLinkContent(
    modifier: Modifier = Modifier,
    email: String?,
    count: String,
    countDownFinished: Boolean,
    onBack: () -> Unit,
    onResend: () -> Unit,
    onForwardAction: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ShoplyTopBar(
            title = stringResource(R.string.send_mail_link),
            onBack = onBack,
        )
        SendEmailLinkComponent(
            message = stringResource(R.string.We_have_sent_password_reset_link_to),
            email = email,
            forwardButtonTitle = stringResource(R.string.ok_i_will_see),
            count = count,
            countDownFinished = countDownFinished,
            sendAgainText = stringResource(R.string.send_link_again),
            onResendAction = onResend,
            onForwardAction = onForwardAction,
            iconSize = 120.dp,
        )
    }
}

@PreviewLightDark
@Composable
private fun SendResetLinkContentPreview() {
    ShoplyTheme {
        SendResetLinkContent(
            email = "username@gmail.com",
            count = "00:45",
            countDownFinished = false,
            onBack = { },
            onResend = { },
            onForwardAction = { }
        )
    }
}
