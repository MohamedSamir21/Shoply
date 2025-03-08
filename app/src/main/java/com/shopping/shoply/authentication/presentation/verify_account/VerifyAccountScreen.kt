package com.shopping.shoply.authentication.presentation.verify_account

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ContinueToHome
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.util.hidePartOfEmailWithStars

@Composable
fun VerifyAccountScreen(
    navController: NavController,
    email: String?,
    count: String,
    countDownFinished: Boolean,
    sendEmailLinkSuccess: UiText?,
    sendEmailLinkError: UiText?,
    onResend: () -> Unit,
    onShowEmailLinkSuccessMessage: () -> Unit,
    onShowEmailLinkErrorMessage: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(sendEmailLinkSuccess) {
        sendEmailLinkSuccess?.let {
            Toast.makeText(
                context,
                it.asString(context),
                Toast.LENGTH_LONG
            ).show()
            onShowEmailLinkSuccessMessage()
        }
    }
    LaunchedEffect(sendEmailLinkError) {
        sendEmailLinkError?.let {
            Toast.makeText(
                context,
                it.asString(context),
                Toast.LENGTH_LONG
            ).show()
            onShowEmailLinkErrorMessage()
        }
    }

    VerifyAccountContent(
        email,
        count,
        countDownFinished = countDownFinished,
        onResend = onResend,
        onContinueToHome = {
            navController.popBackStack()
            navController.navigate(NavRoutes.Home.route)
        }
    )
}

@Composable
private fun VerifyAccountContent(
    email: String?,
    count: String,
    countDownFinished: Boolean,
    onResend: () -> Unit,
    onContinueToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        VerifyYourAccountText()
        Spacer(modifier = Modifier.height(100.dp))
        CheckVerificationEmailText()
        Spacer(modifier = Modifier.height(10.dp))
        email?.let {
            EmailText(it)
        }
        Spacer(modifier = Modifier.height(30.dp))
        SendVerificationLinkSection(count, countDownFinished, onResend)
        Spacer(modifier = Modifier.height(30.dp))
        ContinueToHome(
            onClick = onContinueToHome
        )
    }
}

@Composable
private fun SendVerificationLinkSection(
    count: String,
    countDownFinished: Boolean,
    onResend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SendVerificationLinkText(count, countDownFinished)
        ResendButton(countDownFinished, onResend = onResend)
    }
}

@Composable
fun ResendButton(
    countDownFinished: Boolean,
    onResend: () -> Unit
) {
    AnimatedContent(
        targetState = countDownFinished,
        label = "ResendAnimatedButton"
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "",
            modifier = Modifier
                .padding(start = 10.dp)
                .clickable(
                    enabled = countDownFinished,
                    onClick = onResend
                ),
            tint = if (it)
                MaterialTheme.colorScheme.inversePrimary
            else
                MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun SendVerificationLinkText(
    count: String,
    countDownFinished: Boolean
) {
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.send_verification_link_again_in))
            if (countDownFinished) {
                pushStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            } else {
                pushStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            append(" $count")
        },
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun EmailText(
    email: String
) {
    val emailWithStars = hidePartOfEmailWithStars(email)

    Text(
        text = emailWithStars,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun CheckVerificationEmailText() {
    Text(
        text = stringResource(R.string.check_the_verification_email_link_sent_to),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun VerifyYourAccountText() {
    Text(
        text = stringResource(R.string.verify_your_account),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Preview(
    showSystemUi = true, device = "spec:parent=pixel_5"
)
@Composable
private fun VerifyAccountContentPreview() {
    ShoplyTheme {
        VerifyAccountContent(
            email = "username@gamil.com",
            count = "00:40",
            countDownFinished = false,
            onResend = {

            },
            onContinueToHome = {

            }
        )
    }
}
