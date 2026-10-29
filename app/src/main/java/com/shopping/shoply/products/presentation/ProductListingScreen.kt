package com.shopping.shoply.products.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.catalog.domain.rules.applyFilters
import com.shopping.shoply.catalog.presentation.components.FilterSheet
import com.shopping.shoply.catalog.presentation.components.SortSheet
import com.shopping.shoply.catalog.presentation.components.productGrid
import com.shopping.shoply.catalog.presentation.components.sortChipLabel
import com.shopping.shoply.common.presentation.components.ShoplyChip
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.components.StateMessageTone
import com.shopping.shoply.common.presentation.components.TopBarIconButton
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.home.presentation.preview.HomePreviewData
import kotlinx.coroutines.launch

/** Product Listing wired to [ProductListingViewModel]; snackbars like Home's. */
@Composable
fun ProductListingScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (ProductListingDestination) -> Unit,
    viewModel: ProductListingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navigate by rememberUpdatedState(onNavigate)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductListingEvent.ShowMessage -> launch {
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

    ProductListingContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/** Top bar (category + item count, search), subcategory chips, Filters/Sort, then the grid. */
@Composable
fun ProductListingContent(
    state: ProductListingState,
    onIntent: (ProductListingIntent) -> Unit,
    onNavigate: (ProductListingDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryName = state.category?.name.orEmpty()
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(
            title = categoryName,
            subtitle = state.category?.let { pluralStringResource(R.plurals.categories_item_count, it.itemCount, it.itemCount) },
            onBack = { onNavigate(ProductListingDestination.Back) },
        ) {
            TopBarIconButton(
                icon = ShoplyAppIcons.Search,
                label = stringResource(R.string.cd_search),
                onClick = { onNavigate(ProductListingDestination.Search) },
            )
        }
        when (state.body) {
            ListingBody.Loading -> ListingSkeleton()
            ListingBody.Error -> StateMessage(
                icon = ShoplyAppIcons.WifiOff,
                title = stringResource(R.string.common_offline_title),
                message = stringResource(R.string.common_offline_message),
                actionLabel = stringResource(R.string.common_offline_action),
                onAction = { onIntent(ProductListingIntent.Retry) },
                tone = StateMessageTone.Neutral,
            )
            ListingBody.Empty -> StateMessage(
                icon = ShoplyAppIcons.Grid,
                title = stringResource(R.string.listing_empty_title),
                message = stringResource(R.string.listing_empty_message, categoryName),
                actionLabel = stringResource(R.string.listing_empty_action),
                onAction = { onNavigate(ProductListingDestination.Categories) },
            )
            ListingBody.Content -> LazyColumn(contentPadding = PaddingValues(bottom = ShoplyDimens.gutter)) {
                if (state.subcategories.isNotEmpty()) {
                    item(key = "subcategories") { SubcategoryChips(state, onIntent) }
                }
                item(key = "toolbar") { Toolbar(state, onIntent) }
                if (state.results.isEmpty()) {
                    item(key = "no-matches") {
                        StateMessage(
                            icon = ShoplyAppIcons.Filter,
                            title = stringResource(R.string.filters_no_matches_title),
                            message = stringResource(R.string.filters_no_matches_message),
                            actionLabel = stringResource(R.string.filters_no_matches_action),
                            onAction = { onIntent(ProductListingIntent.ResetFilters) },
                        )
                    }
                } else {
                    productGrid(
                        products = state.results,
                        wishlist = state.wishlist,
                        onClick = { onNavigate(ProductListingDestination.Product(it.id)) },
                        onToggleWishlist = { onIntent(ProductListingIntent.ToggleWishlist(it.id)) },
                        onQuickAdd = { onIntent(ProductListingIntent.QuickAdd(it)) },
                    )
                }
            }
        }
    }

    when (state.sheet) {
        // No Category row: the screen is already one category.
        ListingSheet.FILTERS -> FilterSheet(
            filters = state.filters,
            categories = emptyList(),
            countFor = { applyFilters(state.matches, it, state.sort, state.freeDeliveryStoreIds).size },
            onApply = { onIntent(ProductListingIntent.ApplyFilters(it)) },
            onDismiss = { onIntent(ProductListingIntent.CloseSheet) },
        )
        ListingSheet.SORT -> SortSheet(
            sort = state.sort,
            onSelect = { onIntent(ProductListingIntent.SelectSort(it)) },
            onDismiss = { onIntent(ProductListingIntent.CloseSheet) },
        )
        ListingSheet.NONE -> Unit
    }
}

@Composable
private fun SubcategoryChips(state: ProductListingState, onIntent: (ProductListingIntent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 4.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShoplyChip(
            label = stringResource(R.string.listing_all),
            selected = state.subcategory == null,
            onClick = { onIntent(ProductListingIntent.SelectSubcategory(null)) },
        )
        state.subcategories.forEach { sub ->
            ShoplyChip(
                label = sub,
                selected = state.subcategory == sub,
                onClick = { onIntent(ProductListingIntent.SelectSubcategory(sub)) },
            )
        }
    }
}

@Composable
private fun Toolbar(state: ProductListingState, onIntent: (ProductListingIntent) -> Unit) = Column {
    Row(
        modifier = Modifier.padding(horizontal = ShoplyDimens.gutter),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ShoplyChip(
            label = stringResource(R.string.filters_chip),
            icon = ShoplyAppIcons.Filter,
            count = state.filters.activeCount,
            onClick = { onIntent(ProductListingIntent.OpenSheet(ListingSheet.FILTERS)) },
        )
        ShoplyChip(
            label = sortChipLabel(state.sort),
            trailingIcon = ShoplyAppIcons.ChevronDown,
            onClick = { onIntent(ProductListingIntent.OpenSheet(ListingSheet.SORT)) },
        )
    }
    Text(
        text = pluralStringResource(R.plurals.results_count, state.results.size, state.results.size),
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = ShoplyDimens.gutter, vertical = 14.dp),
    )
}

@Composable
private fun ListingSkeleton() {
    SkeletonPulse(modifier = Modifier.padding(horizontal = ShoplyDimens.gutter, vertical = 8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space4)) {
                    repeat(2) { SkeletonBone(modifier = Modifier.weight(1f).height(200.dp), shape = ShoplyShapes.card) }
                }
            }
        }
    }
}

@StringRes
private fun ListingMessage.textRes(): Int = when (this) {
    ListingMessage.SAVED_TO_WISHLIST -> R.string.snackbar_wishlist_saved
    ListingMessage.REMOVED_FROM_WISHLIST -> R.string.snackbar_wishlist_removed
    ListingMessage.WISHLIST_FAILED -> R.string.snackbar_wishlist_failed
    ListingMessage.ADDED_TO_CART -> R.string.snackbar_cart_added
    ListingMessage.CART_FAILED -> R.string.snackbar_cart_failed
}

private fun ListingMessage.viewTarget(): ProductListingDestination? = when (this) {
    ListingMessage.SAVED_TO_WISHLIST -> ProductListingDestination.Wishlist
    ListingMessage.ADDED_TO_CART -> ProductListingDestination.Cart
    else -> null
}

private val previewProducts = listOf(HomePreviewData.headphones, HomePreviewData.lamp, HomePreviewData.mug)
private val previewContent = ProductListingState(
    body = ListingBody.Content,
    category = HomePreviewData.categories.first(),
    subcategories = listOf("Audio", "Wearables", "Accessories"),
    matches = previewProducts,
    results = previewProducts,
    filters = ProductFilters(minRating = 4.5),
    wishlist = setOf(HomePreviewData.mug.id),
)

@Composable
private fun ListingPreview(state: ProductListingState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductListingContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun ProductListingContentPreview() = ListingPreview(previewContent)

@PreviewLightDark
@Composable
fun ProductListingNoMatchesPreview() = ListingPreview(previewContent.copy(results = emptyList()))

@PreviewLightDark
@Composable
fun ProductListingEmptyPreview() = ListingPreview(previewContent.copy(body = ListingBody.Empty))

@PreviewLightDark
@Composable
fun ProductListingLoadingPreview() = ListingPreview(ProductListingState())

@PreviewLightDark
@Composable
fun ProductListingErrorPreview() = ListingPreview(previewContent.copy(body = ListingBody.Error))
