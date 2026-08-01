package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyShapes

/**
 * Selectable contact-method card (reset-password chooser). Mirrors the handoff
 * `MethodCard` (ui_kits/auth/shoply-screens-2.jsx).
 */
@Composable
fun MethodCard(
    icon: ImageVector,
    kind: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (selected) scheme.primaryContainer else scheme.surfaceVariant
    val borderColor = if (selected) scheme.primary else scheme.outlineVariant

    Surface(
        onClick = onClick,
        shape = ShoplyShapes.card,
        color = container,
        border = BorderStroke(2.dp, borderColor),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = ShoplyDimens.space4),
            horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // leading icon tile
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (selected) scheme.primary else scheme.surface,
                        RoundedCornerShape(12.dp),
                    )
                    .then(
                        if (selected) Modifier
                        else Modifier.border(1.dp, scheme.outlineVariant, RoundedCornerShape(12.dp))
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) scheme.onPrimary else scheme.onSurfaceVariant,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = kind,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // trailing radio-check
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        if (selected) scheme.primary else Color.Transparent,
                        CircleShape,
                    )
                    .border(2.dp, if (selected) scheme.primary else scheme.outline, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        imageVector = ShoplyIcons.Check,
                        contentDescription = null,
                        tint = scheme.onPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
