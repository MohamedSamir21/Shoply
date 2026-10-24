package com.shopping.shoply.product.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.Dots
import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.home.presentation.components.ProductImage

// No photos in v1: the slides are the product glyph at different sizes, like the design.
private val slideCircleRatios = listOf(0.6f, 0.48f, 0.7f, 0.54f)

/**
 * The 300 dp image pager with back, share and wishlist buttons on top, dots and an "n/4"
 * counter below.
 */
@Composable
fun ProductGallery(
    icon: CatalogIcon,
    isSaved: Boolean,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { slideCircleRatios.size }
    Box(modifier = modifier.fillMaxWidth().height(300.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            ProductImage(
                icon = icon,
                cornerRadius = 0.dp,
                circleRatio = slideCircleRatios[page],
                modifier = Modifier.fillMaxSize(),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OverlayButton(ShoplyAppIcons.Back, stringResource(R.string.cd_go_back), onBack)
            Box(modifier = Modifier.weight(1f))
            OverlayButton(ShoplyAppIcons.Share, stringResource(R.string.cd_share), onShare)
            OverlayButton(
                icon = if (isSaved) ShoplyAppIcons.HeartFilled else ShoplyAppIcons.Heart,
                label = stringResource(if (isSaved) R.string.cd_wishlist_remove else R.string.cd_wishlist_add),
                onClick = onToggleWishlist,
                active = isSaved,
            )
        }
        Dots(
            count = slideCircleRatios.size,
            index = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 10.dp)
                .height(26.dp)
                .shoplyCard(CircleShape)
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.product_image_counter, pagerState.currentPage + 1, slideCircleRatios.size),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** 44 dp surface circle with a card shadow, so it reads over the image. */
@Composable
private fun OverlayButton(icon: ImageVector, label: String, onClick: () -> Unit, active: Boolean = false) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .shoplyCard(CircleShape)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(21.dp),
        )
    }
}

@PreviewLightDark
@Composable
fun ProductGalleryPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductGallery(icon = CatalogIcon.HEADPHONES, isSaved = true, onBack = {}, onShare = {}, onToggleWishlist = {})
        }
    }
}
