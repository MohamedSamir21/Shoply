package com.shopping.shoply.authentication.presentation.result

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.common.presentation.components.ResultBadge
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/**
 * Terminal result states from the auth flow. [ok] drives the confetti badge
 * colour and the title hue, mirroring the handoff Success / Error screens
 * (ui_kits/auth/shoply-screens-2.jsx).
 */
enum class ResultPurpose(
    val ok: Boolean,
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    @StringRes val buttonRes: Int,
) {
    PASSWORD_RESET(
        ok = true,
        titleRes = R.string.congratulations,
        messageRes = R.string.password_reset_success_message,
        buttonRes = R.string.continue_to_home,
    ),
    EMAIL_VERIFIED(
        ok = true,
        titleRes = R.string.congratulations,
        messageRes = R.string.success_subtitle,
        buttonRes = R.string.continue_to_home,
    ),
    VERIFY_FAILED(
        ok = false,
        titleRes = R.string.oops,
        messageRes = R.string.error_subtitle,
        buttonRes = R.string.continue_to_home,
    );
}

/**
 * Success / error result rendered as a dialog card floating over the screen that
 * launched it, with a spring "pop" entrance. Mirrors the handoff `ResultDialog`
 * (ui_kits/auth/shoply-screens-2.jsx).
 */
@Composable
fun ResultScreen(
    purpose: ResultPurpose,
    onContinue: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleColor = if (purpose.ok) MaterialTheme.shoplyExtra.green else MaterialTheme.shoplyExtra.red

    // Drive the pop entrance once, after the dialog is composed.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            // Tapping the scrim outside the card dismisses, matching the design.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose,
            )
            .padding(ShoplyDimens.gutter),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ) + fadeIn(),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                ),
                shadowElevation = 24.dp,
                modifier = Modifier
                    .widthIn(max = 312.dp)
                    .fillMaxWidth()
                    // Swallow taps on the card so they don't fall through to the scrim.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = ShoplyDimens.gutter,
                        end = ShoplyDimens.gutter,
                        top = ShoplyDimens.space4,
                        bottom = ShoplyDimens.gutter,
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(34.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = ShoplyIcons.XCircle,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp),
                        )
                    }

                    ResultBadge(ok = purpose.ok, size = 132.dp)

                    Spacer(Modifier.height(ShoplyDimens.space3))
                    Text(
                        text = stringResource(purpose.titleRes),
                        style = MaterialTheme.typography.titleLarge,
                        color = titleColor,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(ShoplyDimens.space2))
                    Text(
                        text = stringResource(purpose.messageRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(ShoplyDimens.space5))
                    ShoplyButton(
                        label = stringResource(purpose.buttonRes),
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ResultScreenSuccessPreview() {
    ShoplyTheme {
        ResultScreen(
            purpose = ResultPurpose.PASSWORD_RESET,
            onContinue = {},
            onClose = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}

@PreviewLightDark
@Composable
private fun ResultScreenErrorPreview() {
    ShoplyTheme {
        ResultScreen(
            purpose = ResultPurpose.VERIFY_FAILED,
            onContinue = {},
            onClose = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}