package com.shopping.shoply.authentication.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.util.hidePartOfEmailWithStars

/**
 * Body for the Verify-account / Send-reset-link screens: a soft orange mail
 * badge, the masked destination address, a Resend countdown, and the primary
 * CTA pinned to the bottom. Mirrors the handoff Verify / SendMail screens
 * (ui_kits/auth/shoply-screens-1.jsx & 2.jsx).
 *
 * [verticallyCentered] centres the badge/message/resend block between the top
 * bar and the CTA (Verify screen); otherwise the block sits near the top
 * (Send-mail screen). [pulse] adds the expanding notification rings around the
 * mail badge.
 */
@Composable
fun SendEmailLinkComponent(
    message: String,
    email: String?,
    forwardButtonTitle: String,
    count: String,
    countDownFinished: Boolean,
    sendAgainText: String,
    onResendAction: () -> Unit,
    onForwardAction: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 96.dp,
    resendLabel: String = stringResource(R.string.resend),
    verticallyCentered: Boolean = false,
    pulse: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ShoplyDimens.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (verticallyCentered) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                MailLinkBody(
                    message = message,
                    email = email,
                    count = count,
                    countDownFinished = countDownFinished,
                    sendAgainText = sendAgainText,
                    resendLabel = resendLabel,
                    onResendAction = onResendAction,
                    iconSize = iconSize,
                    pulse = pulse,
                )
            }
        } else {
            Spacer(Modifier.height(ShoplyDimens.space5))
            MailLinkBody(
                message = message,
                email = email,
                count = count,
                countDownFinished = countDownFinished,
                sendAgainText = sendAgainText,
                resendLabel = resendLabel,
                onResendAction = onResendAction,
                iconSize = iconSize,
                pulse = pulse,
            )
            Spacer(Modifier.weight(1f))
        }

        ShoplyButton(
            label = forwardButtonTitle,
            onClick = onForwardAction,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(ShoplyDimens.space5))
    }
}

@Composable
private fun ColumnScope.MailLinkBody(
    message: String,
    email: String?,
    count: String,
    countDownFinished: Boolean,
    sendAgainText: String,
    resendLabel: String,
    onResendAction: () -> Unit,
    iconSize: Dp,
    pulse: Boolean,
) {
    PulsingMailBadge(iconSize = iconSize, pulse = pulse)

    Spacer(Modifier.height(ShoplyDimens.space8))

    Text(
        text = message,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    email?.let {
        Spacer(Modifier.height(ShoplyDimens.space2))
        Text(
            text = hidePartOfEmailWithStars(it),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }

    Spacer(Modifier.height(ShoplyDimens.space6))

    SendAgainComponent(
        sendText = sendAgainText,
        count = count,
        countDownFinished = countDownFinished,
        resendLabel = resendLabel,
        onResendAction = onResendAction,
    )
}

/**
 * The soft-orange mail badge, optionally wrapped in two staggered expanding rings
 * that fade out — the "notification" animation from the handoff Verify screen
 * (`.pulse-ring`, 2.4s, second ring delayed 1.2s).
 */
@Composable
private fun PulsingMailBadge(iconSize: Dp, pulse: Boolean) {
    Box(
        modifier = Modifier.size(iconSize),
        contentAlignment = Alignment.Center,
    ) {
        if (pulse) {
            val transition = rememberInfiniteTransition(label = "pulse")
            repeat(2) { ring ->
                val progress by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2400, easing = LinearEasing),
                        initialStartOffset = StartOffset(ring * 1200),
                    ),
                    label = "ring$ring",
                )
                // 0% -> scale .78 / opacity .55, 70% -> scale 1.4 / opacity 0.
                val ringScale = 0.78f + (1.4f - 0.78f) * progress
                val ringAlpha = (0.55f * (1f - progress / 0.7f)).coerceAtLeast(0f)
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .scale(ringScale)
                        .alpha(ringAlpha)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                )
            }
        }

        Box(
            modifier = Modifier
                .size(iconSize)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ShoplyIcons.Mail,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(iconSize * 0.45f),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun SendEmailLinkPreview() {
    ShoplyTheme {
        SendEmailLinkComponent(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            message = stringResource(R.string.check_email_link_sent_to),
            email = "username@gmail.com",
            forwardButtonTitle = stringResource(R.string.continue_to_home),
            count = "00:40",
            countDownFinished = false,
            sendAgainText = stringResource(R.string.send_verification_link_again_in),
            onResendAction = {},
            onForwardAction = {},
            verticallyCentered = true,
            pulse = true,
        )
    }
}
