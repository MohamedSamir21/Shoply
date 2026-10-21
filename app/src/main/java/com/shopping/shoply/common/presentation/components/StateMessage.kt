package com.shopping.shoply.common.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.darkRing

/** Colour of the icon circle: orange for "nothing here" states, grey for errors like Offline. */
enum class StateMessageTone { Brand, Neutral }

/**
 * Centered full-area message: icon in a 112 dp soft circle, title, message and one button.
 * Used for Home's Empty ("No Stores Nearby Yet", [StateMessageTone.Brand]) and Offline
 * ("You're Offline", [StateMessageTone.Neutral]) states, and reusable by any list screen.
 */
@Composable
fun StateMessage(
    icon: ImageVector,
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    tone: StateMessageTone = StateMessageTone.Brand,
) {
    val scheme = MaterialTheme.colorScheme
    val (circleColor, iconColor) = when (tone) {
        StateMessageTone.Brand -> scheme.primaryContainer to scheme.primary
        StateMessageTone.Neutral -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ShoplyDimens.space8, vertical = ShoplyDimens.space10),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(circleColor)
                .darkRing(CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(46.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = scheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = ShoplyDimens.space6)
                .semantics { heading() },
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 23.sp,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = ShoplyDimens.space2)
                .widthIn(max = 270.dp),
        )
        ShoplyButton(
            label = actionLabel,
            onClick = onAction,
            // The button fills its width by default; IntrinsicSize.Max shrinks it to its label.
            modifier = Modifier
                .padding(top = ShoplyDimens.space6)
                .width(IntrinsicSize.Max),
        )
    }
}

@PreviewLightDark
@Composable
fun StateMessageEmptyPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StateMessage(
                icon = ShoplyAppIcons.Pin,
                title = stringResource(R.string.home_empty_title),
                message = stringResource(R.string.home_empty_message),
                actionLabel = stringResource(R.string.home_empty_action),
                onAction = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
fun StateMessageOfflinePreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StateMessage(
                icon = ShoplyAppIcons.WifiOff,
                title = stringResource(R.string.common_offline_title),
                message = stringResource(R.string.common_offline_message),
                actionLabel = stringResource(R.string.common_offline_action),
                onAction = {},
                tone = StateMessageTone.Neutral,
            )
        }
    }
}
