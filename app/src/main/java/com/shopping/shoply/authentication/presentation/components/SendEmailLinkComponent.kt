package com.shopping.shoply.authentication.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.util.hidePartOfEmailWithStars

@Composable
fun SendEmailLinkComponent(
    modifier: Modifier = Modifier,
    title: String,
    forwardButtonTitle: String,
    email: String?,
    count: String,
    countDownFinished: Boolean,
    onResendAction: () -> Unit,
    onForwardAction: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(100.dp))
        ContentCard(
            modifier = Modifier.padding(10.dp),
            email = email,
            count = count,
            forwardButtonTitle = forwardButtonTitle,
            countDownFinished = countDownFinished,
            onResendAction = onResendAction,
            onForwardAction = onForwardAction
        )
    }
}

@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    email: String?,
    count: String,
    countDownFinished: Boolean,
    forwardButtonTitle: String,
    onResendAction: () -> Unit,
    onForwardAction: () -> Unit
) {
    OutlinedCard(
        modifier = modifier
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.check_email_link_sent_to),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            email?.let {
                val emailWithStars = hidePartOfEmailWithStars(email)

                Text(
                    text = emailWithStars,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SendLinkText(count, countDownFinished)
                ResendButton(countDownFinished, onResend = onResendAction)
            }
            Spacer(modifier = Modifier.height(30.dp))
            ShoplyButton(
                label = forwardButtonTitle,
                onClick = onForwardAction
            )
        }
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
fun SendLinkText(
    count: String,
    countDownFinished: Boolean
) {
    Text(
        text = buildAnnotatedString {
            pushStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            append(stringResource(R.string.send_link_again))
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
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            append(" $count")
        },
        style = MaterialTheme.typography.bodyMedium
    )
}

@PreviewLightDark
@Composable
private fun VerifyAccountContentPreview() {
    ShoplyTheme {
        SendEmailLinkComponent(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surface
            ),
            title = stringResource(R.string.send_email_link),
            forwardButtonTitle = stringResource(R.string.proceed),
            email = "username@gamil.com",
            count = "00:40",
            countDownFinished = true,
            onResendAction = {

            },
            onForwardAction = {

            }
        )
    }
}
