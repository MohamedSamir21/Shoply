package com.shopping.shoply.notifications.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.notifications.domain.model.Notification
import com.shopping.shoply.notifications.domain.model.NotificationType
import com.shopping.shoply.notifications.presentation.preview.NotificationsPreviewData

/**
 * One notification: 44 dp icon box by type, title + [timeLabel], body. Unread: bolder title,
 * orange dot and a faint tint behind the row.
 */
@Composable
fun NotificationRow(
    notification: Notification,
    timeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unread = !notification.read
    val unreadLabel = stringResource(R.string.cd_notification_unread)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (unread) unreadTint() else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = ShoplyDimens.gutter, vertical = 14.dp)
            .semantics(mergeDescendants = true) { if (unread) contentDescription = unreadLabel },
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = notification.type.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(21.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 14.5.sp,
                    fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (unread) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                }
            }
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}

// The design's unread tint: a whisper of orange in light, of white in dark.
@Composable
private fun unreadTint(): Color =
    if (MaterialTheme.shoplyExtra.isDark) Color.White.copy(alpha = 0.024f)
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)

private val NotificationType.icon: ImageVector
    get() = when (this) {
        NotificationType.ORDER_CONFIRMED -> ShoplyAppIcons.Store
        NotificationType.ORDER_ON_THE_WAY -> ShoplyAppIcons.Truck
        NotificationType.ORDER_DELIVERED -> ShoplyAppIcons.Package
        NotificationType.PROMO -> ShoplyAppIcons.Tag
        NotificationType.PRICE_DROP -> ShoplyAppIcons.Heart
        NotificationType.PAYMENT_METHOD -> ShoplyAppIcons.Card
        NotificationType.GENERAL -> ShoplyAppIcons.Bell
    }

/** An unread row above a read one. */
@PreviewLightDark
@Composable
fun NotificationRowPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                NotificationRow(notification = NotificationsPreviewData.onTheWay, timeLabel = "10:34 AM", onClick = {})
                NotificationRow(notification = NotificationsPreviewData.priceDrop, timeLabel = "Yesterday", onClick = {})
            }
        }
    }
}
