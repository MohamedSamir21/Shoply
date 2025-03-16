package com.shopping.shoply.authentication.presentation.reset_password.send_rest_link

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.SendEmailLinkComponent
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun SendResetLinkScreen(
    navController: NavController,
    email: String
) {
    val viewModel: SendRestLinkViewModel = hiltViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val countDownTime = state.value.countDownTime
    val countDownFinished = state.value.countDownFinished
    val sendEmailLinkSuccess = state.value.sendEmailLinkSuccessMessage
    val sendEmailLinkError = state.value.sendEmailLinkError
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(SendRestLinkIntent.OnFirstPopUp(email))

    }
    LaunchedEffect(sendEmailLinkSuccess) {
        sendEmailLinkSuccess?.let {
            Toast.makeText(
                context,
                it.asString(context),
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetEmailLinkSuccessMessage()
        }
    }
    LaunchedEffect(sendEmailLinkError) {
        sendEmailLinkError?.let {
            Toast.makeText(
                context,
                it.asString(context),
                Toast.LENGTH_LONG
            ).show()
            viewModel.resetEmailLinkErrorMessage()
        }
    }

    SendRestLinkContent(
        email = email,
        count = countDownTime,
        countDownFinished = countDownFinished,
        onResend = {
            viewModel.onIntent(SendRestLinkIntent.Resend)
        },
        onForwardAction = {
            navController.popBackStack()
            navController.navigate(NavRoutes.SignIn.route)
        }
    )

}

@Composable
private fun SendRestLinkContent(
    modifier: Modifier = Modifier,
    email: String?,
    count: String,
    countDownFinished: Boolean,
    onResend: () -> Unit,
    onForwardAction: () -> Unit
) {
    SendEmailLinkComponent(
        modifier = modifier,
        title = stringResource(R.string.send_rest_link),
        forwardButtonTitle = stringResource(R.string.ok_i_will_see),
        email = email,
        count = count,
        countDownFinished = countDownFinished,
        onResendAction = onResend,
        onForwardAction = onForwardAction
    )
}

@Preview
@Composable
private fun SendRestLinkContentPreview() {
    ShoplyTheme {
        SendRestLinkContent(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            email = "username@gmail.com",
            count = "00:45",
            countDownFinished = false,
            onResend = { },
            onForwardAction = { }
        )
    }
}