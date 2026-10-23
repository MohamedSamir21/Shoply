package com.shopping.shoply.wishlist.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.components.StateMessageTone
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.home.presentation.preview.HomePreviewData
import com.shopping.shoply.wishlist.presentation.components.WishlistItemCard
import kotlinx.coroutines.launch

/**
 * The Wishlist. Snackbars go to [snackbarHostState] (from MainShell, so they sit above the bottom
 * navigation); "Moved to cart" has a View action → [WishlistDestination.Cart].
 */
@Composable
fun WishlistScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNavigate: (WishlistDestination) -> Unit,
    viewModel: WishlistViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navigate by rememberUpdatedState(onNavigate)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is WishlistEvent.ShowMessage -> launch {
                    // A new message replaces the one on screen instead of waiting behind it.
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val toCart = event.message == WishlistMessage.MOVED_TO_CART
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(event.message.textRes()),
                        actionLabel = if (toCart) context.getString(R.string.common_view) else null,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed && toCart) navigate(WishlistDestination.Cart)
                }
            }
        }
    }

    WishlistContent(state = state, onIntent = viewModel::onIntent, onBack = onBack, onNavigate = onNavigate)
}

/**
 * The Wishlist without a ViewModel (previews and UI tests use this): top bar ("N saved items"
 * under the title while there's content), then the body by [WishlistState.body]:
 * - Loading → list skeleton
 * - Empty → "Your Wishlist Is Empty" + Explore Products → [WishlistDestination.ExploreProducts]
 * - Offline → "You're Offline" + Retry → [WishlistIntent.Retry]
 * - Content → one [WishlistItemCard] per product.
 */
@Composable
fun WishlistContent(
    state: WishlistState,
    onIntent: (WishlistIntent) -> Unit,
    onBack: () -> Unit,
    onNavigate: (WishlistDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val body = state.body
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(
            title = stringResource(R.string.screen_wishlist),
            subtitle = (body as? WishlistBody.Content)?.let {
                pluralStringResource(R.plurals.wishlist_saved_count, it.products.size, it.products.size)
            },
            onBack = onBack,
        )
        when (body) {
            WishlistBody.Loading -> WishlistSkeleton()
            WishlistBody.Empty -> StateMessage(
                icon = ShoplyAppIcons.Heart,
                title = stringResource(R.string.wishlist_empty_title),
                message = stringResource(R.string.wishlist_empty_message),
                actionLabel = stringResource(R.string.wishlist_empty_action),
                onAction = { onNavigate(WishlistDestination.ExploreProducts) },
            )
            WishlistBody.Offline -> StateMessage(
                icon = ShoplyAppIcons.WifiOff,
                title = stringResource(R.string.common_offline_title),
                message = stringResource(R.string.common_offline_message),
                actionLabel = stringResource(R.string.common_offline_action),
                onAction = { onIntent(WishlistIntent.Retry) },
                tone = StateMessageTone.Neutral,
            )
            is WishlistBody.Content -> LazyColumn(
                contentPadding = PaddingValues(
                    start = ShoplyDimens.gutter,
                    end = ShoplyDimens.gutter,
                    top = ShoplyDimens.space2,
                    bottom = ShoplyDimens.space6,
                ),
                verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            ) {
                items(body.products, key = { it.id }) { product ->
                    WishlistItemCard(
                        product = product,
                        onClick = { onNavigate(WishlistDestination.Product(product.id)) },
                        onRemove = { onIntent(WishlistIntent.Remove(product.id)) },
                        onMoveToCart = { onIntent(WishlistIntent.MoveToCart(product)) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

/** Four card-shaped placeholders: image, two text lines, a price. */
@Composable
private fun WishlistSkeleton() {
    SkeletonPulse(
        modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = ShoplyDimens.space2),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3)) {
            repeat(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shoplyCard()
                        .padding(ShoplyDimens.space3),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    SkeletonBone(modifier = Modifier.size(72.dp), shape = RoundedCornerShape(12.dp))
                    Column(modifier = Modifier.padding(top = ShoplyDimens.space1)) {
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp))
                        SkeletonBone(modifier = Modifier.padding(top = 10.dp).fillMaxWidth(0.5f).height(14.dp))
                        SkeletonBone(modifier = Modifier.padding(top = 14.dp).fillMaxWidth(0.34f).height(16.dp))
                    }
                }
            }
        }
    }
}

private fun WishlistMessage.textRes(): Int = when (this) {
    WishlistMessage.REMOVED -> R.string.snackbar_wishlist_removed
    WishlistMessage.REMOVE_FAILED -> R.string.snackbar_wishlist_failed
    WishlistMessage.MOVED_TO_CART -> R.string.snackbar_cart_moved
    WishlistMessage.CART_FAILED -> R.string.snackbar_cart_failed
}

@Composable
private fun WishlistContentPreview(body: WishlistBody) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            WishlistContent(state = WishlistState(body), onIntent = {}, onBack = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun WishlistFilledPreview() = WishlistContentPreview(
    WishlistBody.Content(
        listOf(HomePreviewData.headphones, HomePreviewData.linenShirt, HomePreviewData.lamp),
    ),
)

@PreviewLightDark
@Composable
fun WishlistEmptyPreview() = WishlistContentPreview(WishlistBody.Empty)

@PreviewLightDark
@Composable
fun WishlistOfflinePreview() = WishlistContentPreview(WishlistBody.Offline)

@PreviewLightDark
@Composable
fun WishlistLoadingPreview() = WishlistContentPreview(WishlistBody.Loading)
