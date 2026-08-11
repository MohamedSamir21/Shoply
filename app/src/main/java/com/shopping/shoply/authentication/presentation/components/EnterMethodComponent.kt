package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.Description
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/**
 * Shared body of the "enter your email" and "enter your phone" reset-password screens: top bar,
 * guiding message, one input and a submit affordance.
 *
 * [isExpandedScreen] picks the submit affordance — an arrow button inline with the input on wide
 * layouts, a full-width button pinned to the bottom otherwise. [menuButton] is the slot the phone
 * screen fills with the country picker; the email screen leaves it empty.
 *
 * While [isVerifyingValue] is true neither affordance can submit again — a progress indicator
 * takes the bottom button's place, and the inline arrow, which stays on screen, is disabled.
 * [errorMessage] is the only thing that reveals the input's supporting line.
 */
@Composable
fun EnterMethodComponent(
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean,
    methodName: String,
    methodMessage: String,
    methodValue: String,
    isVerifyingValue: Boolean,
    errorMessage: UiText? = null,
    inputLabel: String? = null,
    onMethodValueChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    menuButton: @Composable () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ShoplyDimens.gutter),
    ) {
        ShoplyTopBar(
            title = stringResource(R.string.enter) + " $methodName",
            onBack = onBack,
        )
        Spacer(Modifier.height(ShoplyDimens.space2))
        Text(
            text = methodMessage,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(ShoplyDimens.space6))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            menuButton()
            FormTextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = ShoplyDimens.space2),
                value = methodValue,
                onValueChange = onMethodValueChange,
                label = inputLabel,
                showSupportingText = errorMessage != null,
                supportingText = errorMessage?.asString(),
                keyboardOptions = keyboardOptions,
            )
            if (isExpandedScreen) {
                Spacer(Modifier.width(ShoplyDimens.space4))
                ShoplyIconButton(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    icon = ShoplyIcons.ArrowRight,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentDescription = stringResource(R.string.expanded_next),
                    // The compact layout hides its button behind the progress indicator while
                    // verifying; this one stays on screen, so it has to be disabled instead.
                    enabled = !isVerifyingValue,
                    onClick = onNext
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isVerifyingValue) {
                val linearProgress = Description.LINEAR_PROGRESS.asString()
                LinearProgressIndicator(
                    Modifier.semantics { contentDescription = linearProgress })
            } else if (!isExpandedScreen) {
                ShoplyButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(R.string.next),
                    onClick = onNext
                )
            }
        }
        Spacer(Modifier.height(ShoplyDimens.space5))
    }
}

@PreviewLightDark
@Composable
private fun EnterMethodComponentContentPreview() {
    ShoplyTheme {
        EnterMethodComponent(
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            isExpandedScreen = false,
            methodName = stringResource(R.string.email),
            methodMessage = stringResource(R.string.enter_email_guiding_message),
            methodValue = "",
            isVerifyingValue = false,
            inputLabel = stringResource(R.string.email),
            onMethodValueChange = {},
            onBack = {},
            onNext = {}
        )
    }
}
