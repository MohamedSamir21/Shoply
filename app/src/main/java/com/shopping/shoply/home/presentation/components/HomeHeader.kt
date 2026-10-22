package com.shopping.shoply.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.Avatar
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.home.domain.rules.Greeting
import com.shopping.shoply.home.presentation.HomeState
import com.shopping.shoply.home.presentation.preview.HomePreviewData

/**
 * Sections ①–③, always visible whatever the body shows:
 * - avatar (initials, or a person icon when null), greeting + first name, ♡ and 🔔 buttons
 *   (🔔 gets an orange dot and reads "Notifications, unread" when [HomeState.hasUnreadNotifications]);
 * - "Deliver to {addressLine}" row, or "Add delivery address" when null;
 * - read-only search bar with a filter button.
 */
@Composable
fun HomeHeader(
    state: HomeState,
    onWishlistClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAddressClick: () -> Unit,
    onSearchClick: () -> Unit,
    onFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // ① Avatar, greeting, actions
        Row(
            modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = ShoplyDimens.space2),
            horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(initials = state.initials)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(state.greeting.textRes()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = state.firstName,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TonalIconButton(
                icon = ShoplyAppIcons.Heart,
                label = stringResource(R.string.cd_open_wishlist),
                onClick = onWishlistClick,
            )
            TonalIconButton(
                icon = ShoplyAppIcons.Bell,
                label = stringResource(
                    if (state.hasUnreadNotifications) R.string.cd_notifications_unread else R.string.cd_notifications
                ),
                onClick = onNotificationsClick,
                showDot = state.hasUnreadNotifications,
            )
        }

        // ② Delivery address
        Row(
            modifier = Modifier
                .padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 14.dp)
                .clickable(role = Role.Button, onClick = onAddressClick),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ShoplyAppIcons.Pin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.home_deliver_to),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = state.addressLine ?: stringResource(R.string.home_add_address),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Icon(
                imageVector = ShoplyAppIcons.ChevronDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(16.dp),
            )
        }

        // ③ Search bar
        SearchBar(
            onClick = onSearchClick,
            onFiltersClick = onFiltersClick,
            modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 14.dp),
        )
    }
}

/** 44 dp grey circle button; [showDot] adds the orange unread dot at its top end. */
@Composable
private fun TonalIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    showDot: Boolean = false,
) {
    val tonal = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(tonal)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(21.dp),
        )
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 11.dp)
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, tonal, CircleShape),
            )
        }
    }
}

/** Read-only search field: tapping it opens Search; the filter button opens Search with filters. */
@Composable
private fun SearchBar(onClick: () -> Unit, onFiltersClick: () -> Unit, modifier: Modifier = Modifier) {
    val hint = stringResource(R.string.home_search_hint)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(ShoplyDimens.radiusField),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.4.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = hint },
    ) {
        Row(
            modifier = Modifier
                .height(52.dp)
                .padding(start = ShoplyDimens.space4, end = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = ShoplyAppIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp),
            )
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            val filtersLabel = stringResource(R.string.cd_filters)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shoplyCard(RoundedCornerShape(11.dp))
                    .clickable(role = Role.Button, onClick = onFiltersClick)
                    .semantics { contentDescription = filtersLabel },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ShoplyAppIcons.Filter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun Greeting.textRes(): Int = when (this) {
    Greeting.MORNING -> R.string.home_greeting_morning
    Greeting.AFTERNOON -> R.string.home_greeting_afternoon
    Greeting.EVENING -> R.string.home_greeting_evening
    Greeting.NONE -> R.string.home_greeting_none
}

@PreviewLightDark
@Composable
fun HomeHeaderPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeHeader(
                state = HomePreviewData.headerState,
                onWishlistClick = {},
                onNotificationsClick = {},
                onAddressClick = {},
                onSearchClick = {},
                onFiltersClick = {},
                modifier = Modifier.padding(bottom = ShoplyDimens.space4),
            )
        }
    }
}

/** No name (person icon, "there"), no address ("Add delivery address"), nothing unread (no dot). */
@PreviewLightDark
@Composable
fun HomeHeaderBlankPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeHeader(
                state = HomePreviewData.blankHeaderState,
                onWishlistClick = {},
                onNotificationsClick = {},
                onAddressClick = {},
                onSearchClick = {},
                onFiltersClick = {},
                modifier = Modifier.padding(bottom = ShoplyDimens.space4),
            )
        }
    }
}
