package com.shopping.shoply.store.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.common.presentation.util.formatPrice
import com.shopping.shoply.home.presentation.components.ProductImage
import com.shopping.shoply.home.presentation.components.Rating
import com.shopping.shoply.store.domain.rules.OpeningStatus
import com.shopping.shoply.store.presentation.preview.StorePreviewData
import java.util.Locale

/**
 * The grey banner (back, search, favorite) with the store logo overlapping its bottom edge, then
 * the name, "category · distance", rating and opening hours.
 */
@Composable
fun StoreHeader(
    store: Store,
    opening: OpeningStatus?,
    favorite: Boolean,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.surfaceVariant, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .height(128.dp),
        ) {
            StoreBannerButtons(
                favorite = favorite,
                onBack = onBack,
                onSearch = onSearch,
                onToggleFavorite = onToggleFavorite,
            )
            // The logo sits 36 dp below the banner, ringed in the page background.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = ShoplyDimens.gutter)
                    .offset(y = 36.dp)
                    .background(scheme.background, RoundedCornerShape(22.dp))
                    .padding(6.dp),
            ) {
                ProductImage(
                    icon = store.icon,
                    cornerRadius = 18.dp,
                    circleRatio = 0.7f,
                    modifier = Modifier.size(76.dp).shoplyCard(RoundedCornerShape(18.dp)),
                )
            }
        }
        Column(modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 46.dp)) {
            Text(
                text = store.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onBackground,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(R.string.store_location, store.category, formatMiles(store.distanceMiles)),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Rating(rating = store.rating, reviewCount = store.reviewCount, fontSize = 13.5.sp)
                if (opening != null) {
                    Box(modifier = Modifier.size(3.dp).background(MaterialTheme.shoplyExtra.inkFaint, CircleShape))
                    OpeningLabel(opening)
                }
            }
        }
    }
}

@Composable
private fun StoreBannerButtons(
    favorite: Boolean,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BannerButton(ShoplyAppIcons.Back, stringResource(R.string.cd_go_back), onBack)
        Box(modifier = Modifier.weight(1f))
        BannerButton(ShoplyAppIcons.Search, stringResource(R.string.cd_search), onSearch)
        BannerButton(
            icon = if (favorite) ShoplyAppIcons.HeartFilled else ShoplyAppIcons.Heart,
            label = stringResource(if (favorite) R.string.cd_store_favorite_remove else R.string.cd_store_favorite_add),
            onClick = onToggleFavorite,
            active = favorite,
        )
    }
}

/** 44 dp surface-coloured circle on the banner; [active] tints the icon orange. */
@Composable
private fun BannerButton(icon: ImageVector, label: String, onClick: () -> Unit, active: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(ShoplyDimens.touchMin)
            .clip(CircleShape)
            .background(scheme.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) scheme.primary else scheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun OpeningLabel(opening: OpeningStatus) {
    val (text, color) = when (opening) {
        is OpeningStatus.OpenUntil ->
            stringResource(R.string.store_open_until, timeLabel(opening.closesAtMinutes)) to MaterialTheme.shoplyExtra.onGreenSoft
        is OpeningStatus.ClosedOpensAt ->
            stringResource(R.string.store_closed_opens_at, timeLabel(opening.opensAtMinutes)) to MaterialTheme.shoplyExtra.red
    }
    Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
}

/** Delivery time, fee and minimum order in one card, split by hairlines. */
@Composable
fun StoreInfoStrip(store: Store, modifier: Modifier = Modifier) {
    val fee = if (store.deliveryFeeCents == 0L) stringResource(R.string.cart_delivery_free) else formatPrice(store.deliveryFeeCents)
    val items = listOf(
        Triple(ShoplyAppIcons.Clock, stringResource(R.string.store_info_delivery), stringResource(R.string.home_store_eta, store.etaMinutes.first, store.etaMinutes.last)),
        Triple(ShoplyAppIcons.Truck, stringResource(R.string.store_info_fee), fee),
        Triple(ShoplyAppIcons.Bag, stringResource(R.string.store_info_minimum), formatPrice(store.minOrderCents)),
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shoplyCard()
            .padding(vertical = 14.dp),
    ) {
        items.forEachIndexed { index, (icon, label, value) ->
            if (index > 0) {
                Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant))
            }
            Column(
                modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {},
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
    }
}

/** 1260 → "9 PM", 570 → "9:30 AM". */
internal fun timeLabel(minutes: Int): String {
    val hour = (minutes / 60) % 24
    val minute = minutes % 60
    val hour12 = (hour % 12).let { if (it == 0) 12 else it }
    val period = if (hour < 12) "AM" else "PM"
    return if (minute == 0) "$hour12 $period" else String.format(Locale.US, "%d:%02d %s", hour12, minute, period)
}

private fun formatMiles(miles: Double): String = String.format(Locale.US, "%.1f", miles)

@PreviewLightDark
@Composable
fun StoreHeaderPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                // Open, not a favorite
                StoreHeader(
                    store = StorePreviewData.atelier,
                    opening = OpeningStatus.OpenUntil(21 * 60),
                    favorite = false,
                    onBack = {},
                    onSearch = {},
                    onToggleFavorite = {},
                )
                // Closed, favorite
                StoreHeader(
                    store = StorePreviewData.atelier,
                    opening = OpeningStatus.ClosedOpensAt(10 * 60),
                    favorite = true,
                    onBack = {},
                    onSearch = {},
                    onToggleFavorite = {},
                )
                StoreInfoStrip(store = StorePreviewData.atelier, modifier = Modifier.padding(horizontal = ShoplyDimens.gutter))
            }
        }
    }
}
