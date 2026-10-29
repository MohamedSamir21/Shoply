package com.shopping.shoply.store.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.components.StateMessageTone
import com.shopping.shoply.common.presentation.components.TopBarIconButton
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.home.presentation.components.ProductCard
import com.shopping.shoply.store.domain.rules.StoreTab
import com.shopping.shoply.store.presentation.components.StoreHeader
import com.shopping.shoply.store.presentation.components.StoreInfoStrip
import com.shopping.shoply.store.presentation.preview.StorePreviewData
import kotlinx.coroutines.launch

/**
 * Store wired to [StoreViewModel]. Snackbars go to [snackbarHostState] (from MainShell); "View"
 * opens the wishlist after [StoreMessage.SAVED_TO_WISHLIST], the cart after [StoreMessage.ADDED_TO_CART].
 */
@Composable
fun StoreScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (StoreDestination) -> Unit,
    viewModel: StoreViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navigate by rememberUpdatedState(onNavigate)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is StoreEvent.ShowMessage -> launch {
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

    StoreContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * Store without a ViewModel. Content is one `LazyColumn`: header, info strip, tab chips, then the
 * selected tab's products in rows of 2 (or its empty state). Loading and Error keep a back button.
 */
@Composable
fun StoreContent(
    state: StoreState,
    onIntent: (StoreIntent) -> Unit,
    onNavigate: (StoreDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val body = state.body) {
        StoreBody.Loading -> Column(modifier = modifier.fillMaxSize()) {
            BackOnly(onBack = { onNavigate(StoreDestination.Back) })
            StoreSkeleton()
        }
        StoreBody.Error -> Column(modifier = modifier.fillMaxSize()) {
            BackOnly(onBack = { onNavigate(StoreDestination.Back) })
            StateMessage(
                icon = ShoplyAppIcons.WifiOff,
                title = stringResource(R.string.common_offline_title),
                message = stringResource(R.string.common_offline_message),
                actionLabel = stringResource(R.string.common_offline_action),
                onAction = { onIntent(StoreIntent.Retry) },
                tone = StateMessageTone.Neutral,
            )
        }
        is StoreBody.Content -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
        ) {
            item(key = "header", contentType = "header") {
                StoreHeader(
                    store = body.store,
                    opening = state.opening,
                    favorite = state.favorite,
                    onBack = { onNavigate(StoreDestination.Back) },
                    onSearch = { onNavigate(StoreDestination.Search) },
                    onToggleFavorite = { onIntent(StoreIntent.ToggleFavorite) },
                )
            }
            item(key = "info", contentType = "info") {
                StoreInfoStrip(
                    store = body.store,
                    modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 18.dp),
                )
            }
            item(key = "tabs", contentType = "tabs") {
                StoreTabChips(
                    selected = state.tab,
                    onSelect = { onIntent(StoreIntent.SelectTab(it)) },
                    modifier = Modifier.padding(top = 22.dp, bottom = 16.dp),
                )
            }
            if (body.products.isEmpty()) {
                item(key = "empty", contentType = "empty") {
                    if (state.tab == StoreTab.OFFERS) {
                        StateMessage(
                            icon = ShoplyAppIcons.Tag,
                            title = stringResource(R.string.store_offers_empty_title),
                            message = stringResource(R.string.store_offers_empty_message),
                            actionLabel = stringResource(R.string.store_offers_empty_action),
                            onAction = { onIntent(StoreIntent.SelectTab(StoreTab.ALL)) },
                        )
                    } else {
                        StateMessage(
                            icon = ShoplyAppIcons.Store,
                            title = stringResource(R.string.store_empty_title),
                            message = stringResource(R.string.store_empty_message, body.store.name),
                            actionLabel = stringResource(R.string.store_empty_action),
                            onAction = { onNavigate(StoreDestination.Home) },
                        )
                    }
                }
            } else {
                // Rows of 2 inside the one LazyColumn, so the grid scrolls with the header.
                items(
                    items = body.products.chunked(2),
                    key = { row -> "row-" + row.joinToString("-") { it.id } },
                    contentType = { "productRow" },
                ) { row ->
                    ProductRow(row = row, wishlist = state.wishlist, onIntent = onIntent, onNavigate = onNavigate)
                }
            }
        }
    }
}

@Composable
private fun ProductRow(
    row: List<Product>,
    wishlist: Set<String>,
    onIntent: (StoreIntent) -> Unit,
    onNavigate: (StoreDestination) -> Unit,
) {
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
                onClick = { onNavigate(StoreDestination.Product(product.id)) },
                onToggleWishlist = { onIntent(StoreIntent.ToggleWishlist(product.id)) },
                onQuickAdd = { onIntent(StoreIntent.QuickAdd(product)) },
                modifier = Modifier.weight(1f),
                imageHeight = 152.dp,
            )
        }
        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
    }
}

/** All / Top Rated / Offers pills; the selected one is orange-tinted. */
@Composable
private fun StoreTabChips(selected: StoreTab, onSelect: (StoreTab) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = ShoplyDimens.gutter)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space2),
    ) {
        StoreTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .clip(ShoplyShapes.pill)
                    .background(if (isSelected) scheme.primaryContainer else scheme.surface)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) scheme.primary else scheme.outlineVariant,
                        shape = ShoplyShapes.pill,
                    )
                    .selectable(selected = isSelected, role = Role.Tab, onClick = { onSelect(tab) })
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(tab.labelRes()),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun BackOnly(onBack: () -> Unit) {
    TopBarIconButton(
        icon = ShoplyAppIcons.Back,
        label = stringResource(R.string.cd_go_back),
        onClick = onBack,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun StoreSkeleton() {
    SkeletonPulse(modifier = Modifier.padding(horizontal = ShoplyDimens.gutter)) {
        Column {
            SkeletonBone(modifier = Modifier.fillMaxWidth().height(128.dp), shape = ShoplyShapes.card)
            SkeletonBone(modifier = Modifier.padding(top = 20.dp).fillMaxWidth(0.5f).height(20.dp))
            SkeletonBone(modifier = Modifier.padding(top = 10.dp).fillMaxWidth(0.4f).height(13.dp))
            SkeletonBone(modifier = Modifier.padding(top = 18.dp).fillMaxWidth().height(78.dp), shape = ShoplyShapes.card)
            Row(modifier = Modifier.padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space4)) {
                repeat(2) { SkeletonBone(modifier = Modifier.weight(1f).height(152.dp), shape = ShoplyShapes.card) }
            }
        }
    }
}

@StringRes
private fun StoreTab.labelRes(): Int = when (this) {
    StoreTab.ALL -> R.string.store_tab_all
    StoreTab.TOP_RATED -> R.string.store_tab_top_rated
    StoreTab.OFFERS -> R.string.store_tab_offers
}

@StringRes
private fun StoreMessage.textRes(): Int = when (this) {
    StoreMessage.SAVED_TO_WISHLIST -> R.string.snackbar_wishlist_saved
    StoreMessage.REMOVED_FROM_WISHLIST -> R.string.snackbar_wishlist_removed
    StoreMessage.WISHLIST_FAILED -> R.string.snackbar_wishlist_failed
    StoreMessage.ADDED_TO_CART -> R.string.snackbar_cart_added
    StoreMessage.CART_FAILED -> R.string.snackbar_cart_failed
    StoreMessage.FAVORITE_FAILED -> R.string.snackbar_store_favorite_failed
}

/** Where the snackbar's "View" goes, or null when the message has no action. */
private fun StoreMessage.viewTarget(): StoreDestination? = when (this) {
    StoreMessage.SAVED_TO_WISHLIST -> StoreDestination.Wishlist
    StoreMessage.ADDED_TO_CART -> StoreDestination.Cart
    else -> null
}

@Composable
private fun StoreContentPreview(state: StoreState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StoreContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun StoreContentPreviewContent() = StoreContentPreview(
    StorePreviewData.state(StoreBody.Content(StorePreviewData.atelier, StorePreviewData.products)),
)

@PreviewLightDark
@Composable
fun StoreContentPreviewNoOffers() = StoreContentPreview(
    StorePreviewData.state(StoreBody.Content(StorePreviewData.atelier, emptyList())).copy(tab = StoreTab.OFFERS),
)

@PreviewLightDark
@Composable
fun StoreContentPreviewEmpty() = StoreContentPreview(
    StorePreviewData.state(StoreBody.Content(StorePreviewData.atelier, emptyList())),
)

@PreviewLightDark
@Composable
fun StoreContentPreviewLoading() = StoreContentPreview(StorePreviewData.state(StoreBody.Loading))

@PreviewLightDark
@Composable
fun StoreContentPreviewError() = StoreContentPreview(StorePreviewData.state(StoreBody.Error))
