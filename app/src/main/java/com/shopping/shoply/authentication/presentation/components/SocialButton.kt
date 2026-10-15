package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes

/**
 * Social sign-in button — 54dp tall, 14dp radius, warm filled container with a
 * hairline border. `full` puts the brand icon on the left with the label
 * optically centered; `compact` centers the icon + label together.
 * Mirrors the handoff `SocialButton` (ui_kits/auth/shoply-ui.jsx).
 */
@Composable
fun SocialButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = ShoplyShapes.field,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ShoplyDimens.socialHeight),
            contentAlignment = Alignment.Center,
        ) {
            if (compact) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text(text = label, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                Image(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = ShoplyDimens.space5)
                        .size(22.dp),
                )
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
