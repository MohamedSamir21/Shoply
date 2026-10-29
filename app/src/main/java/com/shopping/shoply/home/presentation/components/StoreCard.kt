package com.shopping.shoply.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.home.presentation.preview.HomePreviewData
import java.util.Locale

/**
 * 248 dp rail card: 56 dp icon thumbnail, name, category, "★ 4.8 · 25–35 min".
 * The ETA comes from [Store.etaMinutes] ("25–35 min", with an en dash).
 */
@Composable
fun StoreCard(
    store: Store,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDistance: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .width(248.dp)
            .shoplyCard()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(ShoplyDimens.space3),
        horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductImage(
            icon = store.icon,
            modifier = Modifier.size(56.dp),
            cornerRadius = 14.dp,
            circleRatio = 0.72f,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = store.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (showDistance) {
                    stringResource(R.string.store_location, store.category, String.format(Locale.US, "%.1f", store.distanceMiles))
                } else {
                    store.category
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
            Row(
                modifier = Modifier.padding(top = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Rating(rating = store.rating, reviewCount = null, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(MaterialTheme.shoplyExtra.inkFaint, CircleShape),
                )
                Text(
                    text = stringResource(R.string.home_store_eta, store.etaMinutes.first, store.etaMinutes.last),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
        trailing?.invoke()
    }
}

@PreviewLightDark
@Composable
fun StoreCardPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StoreCard(
                store = HomePreviewData.voltElectronics,
                onClick = {},
                modifier = Modifier.padding(ShoplyDimens.gutter),
            )
        }
    }
}
