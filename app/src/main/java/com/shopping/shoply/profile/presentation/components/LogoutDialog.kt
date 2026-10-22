package com.shopping.shoply.profile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/** "Log Out?" confirmation. Back, a tap outside, or Cancel calls [onDismiss]. */
@Composable
fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        LogoutDialogCard(onConfirm = onConfirm, onDismiss = onDismiss)
    }
}

/** The dialog's card on its own, so previews can show it without a window. */
@Composable
fun LogoutDialogCard(onConfirm: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val extra = MaterialTheme.shoplyExtra
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .widthIn(max = 312.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(start = ShoplyDimens.space6, end = ShoplyDimens.space6, top = 28.dp, bottom = ShoplyDimens.space5),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(extra.redSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ShoplyAppIcons.Logout,
                contentDescription = null,
                tint = extra.red,
                modifier = Modifier.size(34.dp),
            )
        }
        Text(
            text = stringResource(R.string.profile_logout_title),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(top = 18.dp)
                .semantics { heading() },
        )
        Text(
            text = stringResource(R.string.profile_logout_message),
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.5.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = ShoplyDimens.space2),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space1),
        ) {
            DialogButton(
                label = stringResource(R.string.profile_log_out),
                container = extra.red,
                content = Color.White,
                onClick = onConfirm,
            )
            DialogButton(
                label = stringResource(R.string.cancel),
                container = Color.Transparent,
                content = MaterialTheme.colorScheme.onSurface,
                onClick = onDismiss,
            )
        }
    }
}

/** Full-width 56 dp button; the design's red confirm and plain Cancel. */
@Composable
private fun DialogButton(label: String, container: Color, content: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = ShoplyShapes.button,
        color = container,
        contentColor = content,
        modifier = Modifier
            .fillMaxWidth()
            .height(ShoplyDimens.controlHeight),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@PreviewLightDark
@Composable
fun LogoutDialogCardPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LogoutDialogCard(onConfirm = {}, onDismiss = {}, modifier = Modifier.padding(ShoplyDimens.gutter))
        }
    }
}
