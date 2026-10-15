package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/**
 * "<send again> (mm:ss)" copy with a textual Resend affordance that lights up
 * orange once the countdown finishes. Mirrors the verify / send-link footer in
 * the handoff (ui_kits/auth/shoply-screens-1.jsx).
 */
@Composable
fun SendAgainComponent(
    modifier: Modifier = Modifier,
    sendText: String,
    count: String,
    countDownFinished: Boolean,
    onResendAction: () -> Unit,
    resendLabel: String,
    showResendButtonWhileCountingDown: Boolean = true,
    symbolAroundCount: Pair<String, String> = Pair("(", ")")
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showResendButtonWhileCountingDown) {
            SendVerificationTextWithCountDown(sendText, countDownFinished, count, symbolAroundCount)
            ResendButton(resendLabel, countDownFinished, onResendAction)
        } else {
            if (countDownFinished) {
                ResendButton(resendLabel, true, onResendAction)
            } else {
                SendVerificationTextWithCountDown(sendText, false, count, symbolAroundCount)
            }
        }
    }
}

@Composable
private fun ResendButton(
    resendLabel: String,
    countDownFinished: Boolean,
    onResendAction: () -> Unit
) {
    Text(
        text = resendLabel,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = if (countDownFinished) MaterialTheme.colorScheme.primary
        else MaterialTheme.shoplyExtra.inkFaint,
        modifier = Modifier
            .padding(start = 2.dp)
            .clickable(enabled = countDownFinished, onClick = onResendAction),
    )
}

@Composable
private fun SendVerificationTextWithCountDown(
    sendText: String,
    countDownFinished: Boolean,
    count: String,
    symbolAroundCount: Pair<String, String> = Pair("(", ")")
) {
    val (prefixSymbol, suffixSymbol) = symbolAroundCount

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                append(sendText)
            }
            if (!countDownFinished) {
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                ) {
                    append(" $prefixSymbol$count$suffixSymbol")
                }
            }
        },
        style = MaterialTheme.typography.labelLarge,
    )
}

@PreviewLightDark
@Composable
private fun SendAgainPreview() {
    ShoplyTheme {
        SendAgainComponent(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            sendText = "send verification link again",
            count = "00:55",
            countDownFinished = false,
            resendLabel = "Resend",
            onResendAction = {},
        )
    }
}
