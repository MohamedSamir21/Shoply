package com.shopping.shoply.categories.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.categories.presentation.components.CategoriesSkeleton
import com.shopping.shoply.categories.presentation.components.CategoryCard
import com.shopping.shoply.categories.presentation.preview.CategoriesPreviewData
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.components.StateMessageTone
import com.shopping.shoply.common.presentation.components.TopBarIconButton
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/** The Categories tab wired to [CategoriesViewModel]. */
@Composable
fun CategoriesScreen(
    onNavigate: (CategoriesDestination) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CategoriesContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * Categories without a ViewModel (previews and UI tests use this). Top bar ("Categories" + search),
 * then the body by [state]:
 * - Loading → [CategoriesSkeleton]
 * - Empty → "Nothing Here Yet" + Change Address → [CategoriesDestination.Addresses]
 * - Offline → "You're Offline" + Retry → [CategoriesIntent.Retry]
 * - Content → 2-column grid of [CategoryCard]s; a tap → [CategoriesDestination.ProductListing].
 */
@Composable
fun CategoriesContent(
    state: CategoriesState,
    onIntent: (CategoriesIntent) -> Unit,
    onNavigate: (CategoriesDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(title = stringResource(R.string.categories_title), centered = false) {
            TopBarIconButton(
                icon = ShoplyAppIcons.Search,
                label = stringResource(R.string.cd_search),
                onClick = { onNavigate(CategoriesDestination.Search) },
            )
        }
        when (state) {
            CategoriesState.Loading -> CategoriesSkeleton()
            // Scrollable so the message and its button stay reachable on short or landscape screens.
            CategoriesState.Empty -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                StateMessage(
                    icon = ShoplyAppIcons.Grid,
                    title = stringResource(R.string.categories_empty_title),
                    message = stringResource(R.string.categories_empty_message),
                    actionLabel = stringResource(R.string.categories_empty_action),
                    onAction = { onNavigate(CategoriesDestination.Addresses) },
                )
            }
            CategoriesState.Offline -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                StateMessage(
                    icon = ShoplyAppIcons.WifiOff,
                    title = stringResource(R.string.common_offline_title),
                    message = stringResource(R.string.common_offline_message),
                    actionLabel = stringResource(R.string.common_offline_action),
                    onAction = { onIntent(CategoriesIntent.Retry) },
                    tone = StateMessageTone.Neutral,
                )
            }
            is CategoriesState.Content -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = ShoplyDimens.gutter,
                    end = ShoplyDimens.gutter,
                    top = ShoplyDimens.space2,
                    bottom = ShoplyDimens.gutter,
                ),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(state.categories, key = { it.id }, contentType = { "category" }) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { onNavigate(CategoriesDestination.ProductListing(category.id)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesContentStatePreview(state: CategoriesState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CategoriesContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun CategoriesContentLoadingPreview() = CategoriesContentStatePreview(CategoriesState.Loading)

@PreviewLightDark
@Composable
fun CategoriesContentEmptyPreview() = CategoriesContentStatePreview(CategoriesState.Empty)

@PreviewLightDark
@Composable
fun CategoriesContentOfflinePreview() = CategoriesContentStatePreview(CategoriesState.Offline)

@PreviewLightDark
@Composable
fun CategoriesContentPreview() =
    CategoriesContentStatePreview(CategoriesState.Content(CategoriesPreviewData.categories))
