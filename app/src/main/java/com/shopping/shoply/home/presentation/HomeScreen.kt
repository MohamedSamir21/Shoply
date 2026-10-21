package com.shopping.shoply.home.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.components.StateMessageTone
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.home.domain.model.HomeFeed
import com.shopping.shoply.home.domain.model.PromoTarget
import com.shopping.shoply.home.presentation.components.CategoryTile
import com.shopping.shoply.home.presentation.components.HomeHeader
import com.shopping.shoply.home.presentation.components.HomeSkeleton
import com.shopping.shoply.home.presentation.components.ProductCard
import com.shopping.shoply.home.presentation.components.PromoCarousel
import com.shopping.shoply.home.presentation.components.SectionHeader
import com.shopping.shoply.home.presentation.components.StoreCard
import com.shopping.shoply.home.presentation.preview.HomePreviewData
import kotlinx.coroutines.launch

/**
 * Home wired to [HomeViewModel]: collects [HomeViewModel.state], and turns each
 * [HomeEvent.ShowMessage] into a snackbar on [snackbarHostState].
 *
 * Snackbar "View" action: [HomeMessage.SAVED_TO_WISHLIST] → [HomeDestination.Wishlist],
 * [HomeMessage.ADDED_TO_CART] → [HomeDestination.Cart]. The other messages have no action.
 *
 * [snackbarHostState] comes from [com.shopping.shoply.common.presentation.navigation.MainShell], so
 * the snackbar sits above the bottom navigation.
 */
@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (HomeDestination) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navigate by rememberUpdatedState(onNavigate)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.ShowMessage -> launch {
                    // A new message replaces the one on screen instead of waiting behind it.
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val target = event.message.viewTarget()
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(event.message.textRes()),
                        actionLabel = target?.let { context.getString(R.string.common_view) },
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed && target != null) navigate(target)
                }
            }
        }
    }

    HomeContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * Home without a ViewModel (previews and UI tests use this). One vertical `LazyColumn`:
 * header ①–③, then the body by [HomeState.body]:
 * - Loading → [HomeSkeleton]
 * - Empty → "No Stores Nearby Yet" + Change Address → [HomeDestination.Addresses]
 * - Offline → "You're Offline" + Retry → [HomeIntent.Retry]
 * - Content → promos, categories, stores, popular rail, recommended 2-column grid.
 *
 * An empty section is hidden with its header. Every lazy item has a stable key (its id) and a
 * content type. Rails scroll horizontally inside the one vertical list (no nested vertical scroll).
 */
@Composable
fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    onNavigate: (HomeDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
    ) {
        item(key = "header", contentType = "header") {
            HomeHeader(
                state = state,
                onWishlistClick = { onNavigate(HomeDestination.Wishlist) },
                onNotificationsClick = { onNavigate(HomeDestination.Notifications) },
                onAddressClick = { onNavigate(HomeDestination.Addresses) },
                onSearchClick = { onNavigate(HomeDestination.Search(openFilters = false)) },
                onFiltersClick = { onNavigate(HomeDestination.Search(openFilters = true)) },
            )
        }
        when (val body = state.body) {
            HomeBody.Loading -> item(key = "skeleton", contentType = "skeleton") { HomeSkeleton() }
            HomeBody.Empty -> item(key = "empty", contentType = "message") {
                StateMessage(
                    icon = ShoplyAppIcons.Pin,
                    title = stringResource(R.string.home_empty_title),
                    message = stringResource(R.string.home_empty_message),
                    actionLabel = stringResource(R.string.home_empty_action),
                    onAction = { onNavigate(HomeDestination.Addresses) },
                )
            }
            HomeBody.Offline -> item(key = "offline", contentType = "message") {
                StateMessage(
                    icon = ShoplyAppIcons.WifiOff,
                    title = stringResource(R.string.common_offline_title),
                    message = stringResource(R.string.common_offline_message),
                    actionLabel = stringResource(R.string.common_offline_action),
                    onAction = { onIntent(HomeIntent.Retry) },
                    tone = StateMessageTone.Neutral,
                )
            }
            is HomeBody.Content -> feedSections(body.feed, state.wishlist, onIntent, onNavigate)
        }
    }
}

/** Sections ④–⑧. Each one is skipped, header included, when it has no items. */
private fun LazyListScope.feedSections(
    feed: HomeFeed,
    wishlist: Set<String>,
    onIntent: (HomeIntent) -> Unit,
    onNavigate: (HomeDestination) -> Unit,
) {
    if (feed.promos.isNotEmpty()) {
        item(key = "promos", contentType = "promos") {
            PromoCarousel(promos = feed.promos, onPromoClick = { onNavigate(it.toDestination()) })
        }
    }

    if (feed.categories.isNotEmpty()) {
        item(key = "categories-header", contentType = "sectionHeader") {
            SectionHeader(
                title = stringResource(R.string.home_section_categories),
                onSeeAll = { onNavigate(HomeDestination.AllCategories) },
            )
        }
        item(key = "categories", contentType = "categoryRail") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = ShoplyDimens.gutter),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(feed.categories, key = { it.id }) { category ->
                    CategoryTile(
                        category = category,
                        onClick = { onNavigate(HomeDestination.ProductListing(category.id)) },
                    )
                }
            }
        }
    }

    if (feed.featuredStores.isNotEmpty()) {
        item(key = "stores-header", contentType = "sectionHeader") {
            SectionHeader(
                title = stringResource(R.string.home_section_featured_stores),
                onSeeAll = { onNavigate(HomeDestination.AllStores) },
            )
        }
        item(key = "stores", contentType = "storeRail") {
            LazyRow(
                // Vertical room so the cards' shadows aren't clipped.
                contentPadding = PaddingValues(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 2.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            ) {
                items(feed.featuredStores, key = { it.id }) { store ->
                    StoreCard(store = store, onClick = { onNavigate(HomeDestination.Store(store.id)) })
                }
            }
        }
    }

    if (feed.popular.isNotEmpty()) {
        item(key = "popular-header", contentType = "sectionHeader") {
            SectionHeader(
                title = stringResource(R.string.home_section_popular),
                onSeeAll = { onNavigate(HomeDestination.ProductListing(POPULAR_SEE_ALL_CATEGORY)) },
            )
        }
        item(key = "popular", contentType = "productRail") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = ShoplyDimens.gutter),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(feed.popular, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isSaved = product.id in wishlist,
                        onClick = { onNavigate(HomeDestination.ProductDetails(product.id)) },
                        onToggleWishlist = { onIntent(HomeIntent.ToggleWishlist(product.id)) },
                        onQuickAdd = { onIntent(HomeIntent.QuickAdd(product)) },
                        modifier = Modifier.width(144.dp),
                    )
                }
            }
        }
    }

    if (feed.recommended.isNotEmpty()) {
        item(key = "recommended-header", contentType = "sectionHeader") {
            SectionHeader(title = stringResource(R.string.home_section_recommended), onSeeAll = null)
        }
        // The grid is rows of 2 inside the one LazyColumn, so it scrolls with the page.
        items(
            items = feed.recommended.chunked(2),
            key = { row -> "recommended-" + row.joinToString("-") { it.id } },
            contentType = { "productRow" },
        ) { row ->
            Row(
                modifier = Modifier
                    .padding(horizontal = ShoplyDimens.gutter)
                    .padding(bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space4),
            ) {
                row.forEach { product ->
                    ProductCard(
                        product = product,
                        isSaved = product.id in wishlist,
                        onClick = { onNavigate(HomeDestination.ProductDetails(product.id)) },
                        onToggleWishlist = { onIntent(HomeIntent.ToggleWishlist(product.id)) },
                        onQuickAdd = { onIntent(HomeIntent.QuickAdd(product)) },
                        modifier = Modifier.weight(1f),
                        imageHeight = 152.dp,
                    )
                }
                // An odd last item keeps half the width instead of stretching.
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// Popular's "See all" goes to Electronics until a real "popular" listing exists (spec Q-1).
private const val POPULAR_SEE_ALL_CATEGORY = "electronics"

private fun PromoTarget.toDestination(): HomeDestination = when (this) {
    is PromoTarget.CategoryListing -> HomeDestination.ProductListing(categoryId)
    is PromoTarget.Store -> HomeDestination.Store(storeId)
    PromoTarget.Help -> HomeDestination.Help
}

@StringRes
private fun HomeMessage.textRes(): Int = when (this) {
    HomeMessage.SAVED_TO_WISHLIST -> R.string.snackbar_wishlist_saved
    HomeMessage.REMOVED_FROM_WISHLIST -> R.string.snackbar_wishlist_removed
    HomeMessage.WISHLIST_FAILED -> R.string.snackbar_wishlist_failed
    HomeMessage.ADDED_TO_CART -> R.string.snackbar_cart_added
    HomeMessage.CART_FAILED -> R.string.snackbar_cart_failed
}

/** Where the snackbar's "View" goes, or null when the message has no action. */
private fun HomeMessage.viewTarget(): HomeDestination? = when (this) {
    HomeMessage.SAVED_TO_WISHLIST -> HomeDestination.Wishlist
    HomeMessage.ADDED_TO_CART -> HomeDestination.Cart
    else -> null
}

@Composable
private fun HomeContentStatePreview(body: HomeBody) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeContent(state = HomePreviewData.state(body), onIntent = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun HomeContentLoadingPreview() = HomeContentStatePreview(HomeBody.Loading)

@PreviewLightDark
@Composable
fun HomeContentEmptyPreview() = HomeContentStatePreview(HomeBody.Empty)

@PreviewLightDark
@Composable
fun HomeContentOfflinePreview() = HomeContentStatePreview(HomeBody.Offline)

@PreviewLightDark
@Composable
fun HomeContentPreview() = HomeContentStatePreview(HomeBody.Content(HomePreviewData.feed))

/** The whole Content page, top to bottom (light only), to check every section at once. */
@Preview(name = "Content – full page", heightDp = 2000)
@Composable
fun HomeContentFullPagePreview() = HomeContentStatePreview(HomeBody.Content(HomePreviewData.feed))
