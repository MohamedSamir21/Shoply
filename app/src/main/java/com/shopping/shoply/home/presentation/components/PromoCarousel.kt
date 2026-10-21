package com.shopping.shoply.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.Dots
import com.shopping.shoply.catalog.presentation.CatalogGlyphs
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.darkRing
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.home.domain.model.Promo
import com.shopping.shoply.home.domain.model.PromoArt
import com.shopping.shoply.home.domain.model.PromoTarget
import com.shopping.shoply.home.presentation.preview.HomePreviewData

private val PromoShape = RoundedCornerShape(20.dp)

/**
 * Swipeable promo banners (312 × 148 dp) that snap one at a time, with the next card peeking in
 * and page dots below. The CTA button calls [onPromoClick] with that promo's target.
 *
 * ⭐ Built on `HorizontalPager`; the current page survives rotation (`rememberPagerState` is
 * saveable). Dots are decorative; each page announces "Promotion {n} of {total}".
 */
@Composable
fun PromoCarousel(
    promos: List<Promo>,
    onPromoClick: (PromoTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState { promos.size }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = ShoplyDimens.space5),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(312.dp),
            pageSpacing = ShoplyDimens.space3,
            contentPadding = PaddingValues(horizontal = ShoplyDimens.gutter),
            key = { promos[it].id },
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            val position = stringResource(R.string.home_promo_position, page + 1, promos.size)
            PromoCard(
                promo = promos[page],
                onClick = { onPromoClick(promos[page].target) },
                modifier = Modifier.semantics { stateDescription = position },
            )
        }
        if (promos.size > 1) {
            Dots(
                count = promos.size,
                index = pagerState.currentPage,
                modifier = Modifier
                    .padding(top = ShoplyDimens.space3)
                    .clearAndSetSemantics { },
            )
        }
    }
}

/** One banner: kicker, title and a small CTA on the left; the art in a white circle on the right. */
@Composable
private fun PromoCard(promo: Promo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .clip(PromoShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .darkRing(PromoShape)
            .padding(start = 20.dp, top = 18.dp, end = 16.dp, bottom = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = promo.kicker.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.1.em,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
            )
            Text(
                text = promo.title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 17.5.sp,
                lineHeight = 23.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp),
            )
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = ShoplyDimens.space3),
            ) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = promo.ctaLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(
                    if (MaterialTheme.shoplyExtra.isDark) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surface
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = promo.art.toImageVector(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp),
            )
        }
    }
}

private fun PromoArt.toImageVector(): ImageVector = when (this) {
    PromoArt.TRUCK -> ShoplyAppIcons.Truck
    PromoArt.SPLIT -> ShoplyAppIcons.Split
    PromoArt.PLANT -> CatalogGlyphs.Plant
    PromoArt.UNKNOWN -> ShoplyAppIcons.Bag
}

@PreviewLightDark
@Composable
fun PromoCarouselPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PromoCarousel(
                promos = HomePreviewData.promos,
                onPromoClick = {},
                modifier = Modifier.padding(bottom = ShoplyDimens.space4),
            )
        }
    }
}
