package com.shopping.shoply.search.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.catalog.domain.rules.applyFilters
import com.shopping.shoply.catalog.presentation.components.FilterSheet
import com.shopping.shoply.catalog.presentation.components.SortSheet
import com.shopping.shoply.catalog.presentation.components.productGrid
import com.shopping.shoply.catalog.presentation.components.sortChipLabel
import com.shopping.shoply.common.presentation.components.ShoplyChip
import com.shopping.shoply.common.presentation.components.ShoplySearchField
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.components.StateMessageTone
import com.shopping.shoply.common.presentation.components.TopBarIconButton
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.home.presentation.components.SectionHeader
import com.shopping.shoply.home.presentation.components.StoreCard
import com.shopping.shoply.search.presentation.preview.SearchPreviewData
import kotlinx.coroutines.launch

/** Search wired to [SearchViewModel]; snackbars like Home's ("View" → wishlist / cart). */
@Composable
fun SearchScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (SearchDestination) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navigate by rememberUpdatedState(onNavigate)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchEvent.ShowMessage -> launch {
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

    SearchContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * Search without a ViewModel: the back button and search field, then one of
 * idle (recent, popular, stores) · suggestions while typing · results after Submit.
 * The field takes focus on open unless results are already showing.
 */
@Composable
fun SearchContent(
    state: SearchState,
    onIntent: (SearchIntent) -> Unit,
    onNavigate: (SearchDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val inPreview = LocalInspectionMode.current
    LaunchedEffect(Unit) { if (!state.submitted && !inPreview) focusRequester.requestFocus() }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = ShoplyDimens.gutter, top = 6.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TopBarIconButton(
                icon = ShoplyAppIcons.Back,
                label = stringResource(R.string.cd_go_back),
                onClick = { onNavigate(SearchDestination.Back) },
            )
            ShoplySearchField(
                value = state.query,
                onValueChange = { onIntent(SearchIntent.QueryChanged(it)) },
                placeholder = stringResource(R.string.home_search_hint),
                onSearch = { onIntent(SearchIntent.Submit(state.query)) },
                modifier = Modifier.focusRequester(focusRequester),
            )
        }
        when {
            state.query.isBlank() && !state.submitted -> IdleBody(state, onIntent, onNavigate)
            !state.submitted -> SuggestionsBody(state, onIntent, onNavigate)
            state.catalog == SearchCatalog.Loading -> ResultsSkeleton()
            state.catalog == SearchCatalog.Error -> StateMessage(
                icon = ShoplyAppIcons.WifiOff,
                title = stringResource(R.string.common_offline_title),
                message = stringResource(R.string.common_offline_message),
                actionLabel = stringResource(R.string.common_offline_action),
                onAction = { onIntent(SearchIntent.Retry) },
                tone = StateMessageTone.Neutral,
            )
            state.matches.isEmpty() -> NoResultsBody(state.query, onIntent)
            else -> ResultsBody(state, onIntent, onNavigate)
        }
    }

    when (state.sheet) {
        SearchSheet.FILTERS -> FilterSheet(
            filters = state.filters,
            categories = state.categories,
            countFor = { applyFilters(state.matches, it, state.sort, state.freeDeliveryStoreIds).size },
            onApply = { onIntent(SearchIntent.ApplyFilters(it)) },
            onDismiss = { onIntent(SearchIntent.CloseSheet) },
        )
        SearchSheet.SORT -> SortSheet(
            sort = state.sort,
            onSelect = { onIntent(SearchIntent.SelectSort(it)) },
            onDismiss = { onIntent(SearchIntent.CloseSheet) },
        )
        SearchSheet.NONE -> Unit
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IdleBody(state: SearchState, onIntent: (SearchIntent) -> Unit, onNavigate: (SearchDestination) -> Unit) {
    val popular = stringArrayResource(R.array.search_popular_terms)
    LazyColumn(contentPadding = PaddingValues(bottom = ShoplyDimens.gutter)) {
        if (state.recent.isNotEmpty()) {
            item(key = "recent-header") {
                SectionHeader(
                    title = stringResource(R.string.search_recent),
                    actionLabel = stringResource(R.string.search_clear_all),
                    onSeeAll = { onIntent(SearchIntent.ClearRecent) },
                )
            }
            items(state.recent, key = { "recent-$it" }) { query ->
                SearchRow(
                    icon = ShoplyAppIcons.Clock,
                    text = query,
                    onClick = { onIntent(SearchIntent.Submit(query)) },
                    trailingIcon = ShoplyAppIcons.Close,
                    trailingLabel = stringResource(R.string.cd_remove_recent, query),
                    onTrailing = { onIntent(SearchIntent.RemoveRecent(query)) },
                )
            }
        }
        item(key = "popular") {
            SectionHeader(title = stringResource(R.string.search_popular), onSeeAll = null)
            FlowRow(
                modifier = Modifier.padding(horizontal = ShoplyDimens.gutter),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                popular.forEach { term ->
                    ShoplyChip(label = term, icon = ShoplyAppIcons.Search, onClick = { onIntent(SearchIntent.Submit(term)) })
                }
            }
        }
        if (state.stores.isNotEmpty()) {
            item(key = "stores-header") { SectionHeader(title = stringResource(R.string.search_stores), onSeeAll = null) }
            items(state.stores, key = { "store-${it.id}" }) { store ->
                StoreCard(
                    store = store,
                    onClick = { onNavigate(SearchDestination.Store(store.id)) },
                    modifier = Modifier
                        .padding(horizontal = ShoplyDimens.gutter)
                        .padding(bottom = 10.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SuggestionsBody(state: SearchState, onIntent: (SearchIntent) -> Unit, onNavigate: (SearchDestination) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(top = 4.dp, bottom = ShoplyDimens.gutter)) {
        items(state.suggestedStores, key = { "store-${it.id}" }) { store ->
            SearchRow(
                icon = ShoplyAppIcons.Store,
                text = store.name,
                subtitle = stringResource(R.string.search_store_eta, store.etaMinutes.first, store.etaMinutes.last),
                onClick = { onNavigate(SearchDestination.Store(store.id)) },
                trailingIcon = ShoplyAppIcons.ChevronRight,
            )
        }
        items(state.suggestions, key = { "product-${it.id}" }) { product ->
            SearchRow(
                icon = ShoplyAppIcons.Search,
                text = product.name,
                onClick = { onIntent(SearchIntent.Submit(product.name)) },
                trailingIcon = ShoplyAppIcons.Edit,
                trailingLabel = stringResource(R.string.cd_use_suggestion),
                onTrailing = { onIntent(SearchIntent.QueryChanged(product.name)) },
            )
        }
        item(key = "search-for") {
            SearchRow(
                icon = ShoplyAppIcons.Search,
                text = stringResource(R.string.search_for, state.query.trim()),
                onClick = { onIntent(SearchIntent.Submit(state.query)) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoResultsBody(query: String, onIntent: (SearchIntent) -> Unit) {
    val popular = stringArrayResource(R.array.search_popular_terms)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StateMessage(
            icon = ShoplyAppIcons.Search,
            title = stringResource(R.string.search_no_results_title),
            message = stringResource(R.string.search_no_results_message, query),
            actionLabel = stringResource(R.string.search_no_results_action),
            onAction = { onIntent(SearchIntent.ClearQuery) },
            modifier = Modifier.weight(1f, fill = false),
        )
        Text(
            text = stringResource(R.string.search_try),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        FlowRow(
            modifier = Modifier.padding(horizontal = ShoplyDimens.gutter),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            popular.take(4).forEach { term -> ShoplyChip(label = term, onClick = { onIntent(SearchIntent.Submit(term)) }) }
        }
    }
}

@Composable
private fun ResultsBody(state: SearchState, onIntent: (SearchIntent) -> Unit, onNavigate: (SearchDestination) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = ShoplyDimens.gutter)) {
        item(key = "toolbar") { ResultsToolbar(state, onIntent) }
        if (state.results.isEmpty()) {
            item(key = "no-matches") {
                StateMessage(
                    icon = ShoplyAppIcons.Filter,
                    title = stringResource(R.string.filters_no_matches_title),
                    message = stringResource(R.string.filters_no_matches_message),
                    actionLabel = stringResource(R.string.filters_no_matches_action),
                    onAction = { onIntent(SearchIntent.ResetFilters) },
                )
            }
        } else {
            productGrid(
                products = state.results,
                wishlist = state.wishlist,
                onClick = { onNavigate(SearchDestination.Product(it.id)) },
                onToggleWishlist = { onIntent(SearchIntent.ToggleWishlist(it.id)) },
                onQuickAdd = { onIntent(SearchIntent.QuickAdd(it)) },
            )
        }
    }
}

/** Filters · Sort · quick chips, then the result count. */
@Composable
internal fun ResultsToolbar(state: SearchState, onIntent: (SearchIntent) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = ShoplyDimens.gutter),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ShoplyChip(
                label = stringResource(R.string.filters_chip),
                icon = ShoplyAppIcons.Filter,
                count = state.filters.activeCount,
                onClick = { onIntent(SearchIntent.OpenSheet(SearchSheet.FILTERS)) },
            )
            ShoplyChip(
                label = sortChipLabel(state.sort),
                trailingIcon = ShoplyAppIcons.ChevronDown,
                onClick = { onIntent(SearchIntent.OpenSheet(SearchSheet.SORT)) },
            )
            QuickFilter.entries.forEach { quick ->
                ShoplyChip(
                    label = stringResource(quick.labelRes()),
                    selected = state.filters.isOn(quick),
                    onClick = { onIntent(SearchIntent.ToggleQuickFilter(quick)) },
                )
            }
        }
        Text(
            text = pluralStringResource(R.plurals.results_count, state.results.size, state.results.size),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = ShoplyDimens.gutter, vertical = 14.dp),
        )
    }
}

/** One 52 dp list row: icon, text (+ subtitle), and an optional trailing button. */
@Composable
private fun SearchRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    trailingIcon: ImageVector? = null,
    trailingLabel: String? = null,
    onTrailing: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = ShoplyDimens.gutter, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailingIcon != null) {
            val tint = MaterialTheme.shoplyExtra.inkFaint
            if (onTrailing != null && trailingLabel != null) {
                TopBarIconButton(icon = trailingIcon, label = trailingLabel, onClick = onTrailing)
            } else {
                Icon(trailingIcon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ResultsSkeleton() {
    SkeletonPulse(modifier = Modifier.padding(horizontal = ShoplyDimens.gutter, vertical = 8.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space4)) {
                    repeat(2) { SkeletonBone(modifier = Modifier.weight(1f).heightIn(min = 200.dp), shape = ShoplyShapes.card) }
                }
            }
        }
    }
}

@StringRes
private fun QuickFilter.labelRes(): Int = when (this) {
    QuickFilter.UNDER_50 -> R.string.filters_quick_under_50
    QuickFilter.RATING_4_5 -> R.string.filters_quick_rating
    QuickFilter.FREE_DELIVERY -> R.string.filters_quick_free_delivery
}

@StringRes
private fun SearchMessage.textRes(): Int = when (this) {
    SearchMessage.SAVED_TO_WISHLIST -> R.string.snackbar_wishlist_saved
    SearchMessage.REMOVED_FROM_WISHLIST -> R.string.snackbar_wishlist_removed
    SearchMessage.WISHLIST_FAILED -> R.string.snackbar_wishlist_failed
    SearchMessage.ADDED_TO_CART -> R.string.snackbar_cart_added
    SearchMessage.CART_FAILED -> R.string.snackbar_cart_failed
}

private fun SearchMessage.viewTarget(): SearchDestination? = when (this) {
    SearchMessage.SAVED_TO_WISHLIST -> SearchDestination.Wishlist
    SearchMessage.ADDED_TO_CART -> SearchDestination.Cart
    else -> null
}

@Composable
private fun SearchContentPreview(state: SearchState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            SearchContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun SearchIdlePreview() = SearchContentPreview(SearchPreviewData.idle)

@PreviewLightDark
@Composable
fun SearchSuggestionsPreview() = SearchContentPreview(SearchPreviewData.typing)

@PreviewLightDark
@Composable
fun SearchResultsPreview() = SearchContentPreview(SearchPreviewData.results)

@PreviewLightDark
@Composable
fun SearchNoResultsPreview() = SearchContentPreview(SearchPreviewData.results.copy(query = "zzz", matches = emptyList(), results = emptyList()))

@PreviewLightDark
@Composable
fun SearchNoMatchesPreview() = SearchContentPreview(SearchPreviewData.results.copy(results = emptyList()))
