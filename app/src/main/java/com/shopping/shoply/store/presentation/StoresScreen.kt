package com.shopping.shoply.store.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.ShoplyChip
import com.shopping.shoply.common.presentation.components.ShoplySearchField
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
import com.shopping.shoply.home.presentation.components.StoreCard
import com.shopping.shoply.home.presentation.preview.HomePreviewData
import com.shopping.shoply.store.domain.rules.StoreSort
import com.shopping.shoply.store.presentation.preview.StorePreviewData
import kotlinx.coroutines.launch

/** All Stores or Favorite Stores (by the nav arg), wired to [StoresViewModel]. */
@Composable
fun StoresScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (StoresDestination) -> Unit,
    viewModel: StoresViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                StoresEvent.FavoriteFailed -> launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.snackbar_store_favorite_failed),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    StoresContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * All Stores: top bar, store search, Sort chip + category chips, count, then store cards with a
 * heart. Favorite Stores: top bar and the favorite cards only.
 */
@Composable
fun StoresContent(
    state: StoresState,
    onIntent: (StoresIntent) -> Unit,
    onNavigate: (StoresDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val loaded = state.body == StoresBody.Content
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(
            title = stringResource(if (state.favoritesOnly) R.string.screen_favorite_stores else R.string.screen_all_stores),
            subtitle = when {
                !loaded -> null
                state.favoritesOnly -> pluralStringResource(R.plurals.stores_count, state.totalCount, state.totalCount)
                else -> pluralStringResource(R.plurals.stores_near_you, state.totalCount, state.totalCount)
            },
            onBack = { onNavigate(StoresDestination.Back) },
        )
        if (!state.favoritesOnly) {
            ShoplySearchField(
                value = state.query,
                onValueChange = { onIntent(StoresIntent.QueryChanged(it)) },
                placeholder = stringResource(R.string.stores_search_hint),
                modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 2.dp, bottom = 14.dp),
            )
        }
        when (state.body) {
            StoresBody.Loading -> StoresSkeleton()
            StoresBody.Error -> StateMessage(
                icon = ShoplyAppIcons.WifiOff,
                title = stringResource(R.string.common_offline_title),
                message = stringResource(R.string.common_offline_message),
                actionLabel = stringResource(R.string.common_offline_action),
                onAction = { onIntent(StoresIntent.Retry) },
                tone = StateMessageTone.Neutral,
            )
            StoresBody.Empty -> if (state.favoritesOnly) {
                StateMessage(
                    icon = ShoplyAppIcons.Store,
                    title = stringResource(R.string.favorites_empty_title),
                    message = stringResource(R.string.favorites_empty_message),
                    actionLabel = stringResource(R.string.favorites_empty_action),
                    onAction = { onNavigate(StoresDestination.AllStores) },
                )
            } else {
                StateMessage(
                    icon = ShoplyAppIcons.Store,
                    title = stringResource(R.string.stores_none_title),
                    message = stringResource(R.string.stores_none_message),
                    actionLabel = stringResource(R.string.stores_none_action),
                    onAction = { onNavigate(StoresDestination.Addresses) },
                )
            }
            StoresBody.Content -> LazyColumn(contentPadding = PaddingValues(bottom = ShoplyDimens.gutter)) {
                if (!state.favoritesOnly) {
                    item(key = "filters") { StoreFilters(state, onIntent) }
                }
                if (state.stores.isEmpty()) {
                    item(key = "not-found") {
                        StateMessage(
                            icon = ShoplyAppIcons.Search,
                            title = stringResource(R.string.stores_not_found_title),
                            message = if (state.query.isNotBlank()) {
                                stringResource(R.string.stores_not_found_query, state.query.trim())
                            } else {
                                stringResource(R.string.stores_not_found_category)
                            },
                            actionLabel = stringResource(R.string.stores_not_found_action),
                            onAction = { onIntent(StoresIntent.ClearFilters) },
                        )
                    }
                }
                items(state.stores, key = { it.id }) { store ->
                    val favorite = store.id in state.favorites
                    StoreCard(
                        store = store,
                        onClick = { onNavigate(StoresDestination.Store(store.id)) },
                        showDistance = true,
                        trailing = {
                            TopBarIconButton(
                                icon = if (favorite) ShoplyAppIcons.HeartFilled else ShoplyAppIcons.Heart,
                                label = stringResource(if (favorite) R.string.cd_store_unfollow else R.string.cd_store_follow, store.name),
                                onClick = { onIntent(StoresIntent.ToggleFavorite(store.id)) },
                                tint = if (favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        modifier = Modifier
                            .padding(horizontal = ShoplyDimens.gutter)
                            .padding(bottom = 10.dp)
                            .fillMaxWidth()
                            .animateItem(),
                    )
                }
            }
        }
    }

    if (state.sortSheetOpen) {
        StoreSortSheet(
            sort = state.sort,
            onSelect = { onIntent(StoresIntent.SelectSort(it)) },
            onDismiss = { onIntent(StoresIntent.CloseSort) },
        )
    }
}

@Composable
private fun StoreFilters(state: StoresState, onIntent: (StoresIntent) -> Unit) = Column {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = ShoplyDimens.gutter),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShoplyChip(
            label = stringResource(state.sort.labelRes()),
            icon = ShoplyAppIcons.Filter,
            trailingIcon = ShoplyAppIcons.ChevronDown,
            onClick = { onIntent(StoresIntent.OpenSort) },
        )
        Box(modifier = Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outlineVariant))
        ShoplyChip(
            label = stringResource(R.string.stores_all),
            selected = state.category == null,
            onClick = { onIntent(StoresIntent.SelectCategory(null)) },
        )
        state.categories.forEach { category ->
            ShoplyChip(
                label = category,
                selected = state.category == category,
                onClick = { onIntent(StoresIntent.SelectCategory(category)) },
            )
        }
    }
    Text(
        text = pluralStringResource(R.plurals.stores_count, state.stores.size, state.stores.size),
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 14.dp, bottom = 12.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoreSortSheet(sort: StoreSort, onSelect: (StoreSort) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
            Text(
                text = stringResource(R.string.stores_sort_title),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = ShoplyDimens.gutter).padding(bottom = 6.dp),
            )
            Column(modifier = Modifier.selectableGroup()) {
                StoreSort.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .selectable(selected = option == sort, role = Role.RadioButton, onClick = { onSelect(option) })
                            .padding(horizontal = ShoplyDimens.gutter),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(option.labelRes()), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        RadioButton(
                            selected = option == sort,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoresSkeleton() {
    SkeletonPulse(modifier = Modifier.padding(horizontal = ShoplyDimens.gutter)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(4) { SkeletonBone(modifier = Modifier.fillMaxWidth().height(80.dp), shape = ShoplyShapes.card) }
        }
    }
}

@StringRes
private fun StoreSort.labelRes(): Int = when (this) {
    StoreSort.NEAREST -> R.string.stores_sort_nearest
    StoreSort.TOP_RATED -> R.string.stores_sort_top_rated
    StoreSort.FASTEST -> R.string.stores_sort_fastest
}

private val previewStores = listOf(StorePreviewData.atelier, HomePreviewData.voltElectronics, HomePreviewData.fernAndCo)
private val previewAll = StoresState(
    body = StoresBody.Content,
    stores = previewStores,
    totalCount = previewStores.size,
    categories = previewStores.map { it.category },
    favorites = setOf(StorePreviewData.atelier.id),
)

@Composable
private fun StoresPreview(state: StoresState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            StoresContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun AllStoresPreview() = StoresPreview(previewAll)

@PreviewLightDark
@Composable
fun AllStoresNotFoundPreview() = StoresPreview(previewAll.copy(stores = emptyList(), query = "zzz"))

@PreviewLightDark
@Composable
fun AllStoresLoadingPreview() = StoresPreview(StoresState())

@PreviewLightDark
@Composable
fun FavoriteStoresPreview() = StoresPreview(
    StoresState(
        favoritesOnly = true,
        body = StoresBody.Content,
        stores = listOf(StorePreviewData.atelier),
        totalCount = 1,
        favorites = setOf(StorePreviewData.atelier.id),
    ),
)

@PreviewLightDark
@Composable
fun FavoriteStoresEmptyPreview() = StoresPreview(StoresState(favoritesOnly = true, body = StoresBody.Empty))
