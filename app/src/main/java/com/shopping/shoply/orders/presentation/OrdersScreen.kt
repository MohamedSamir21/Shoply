package com.shopping.shoply.orders.presentation

import android.content.Context
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.components.SegmentedTab
import com.shopping.shoply.common.presentation.components.SegmentedTabs
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.orders.domain.rules.isActive
import com.shopping.shoply.orders.presentation.components.OrderCard
import com.shopping.shoply.orders.presentation.preview.OrdersPreviewData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** The Orders tab. Snackbars go to [snackbarHostState] (from MainShell, above the bottom navigation). */
@Composable
fun OrdersScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (OrdersDestination) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event -> handleOrdersEvent(event, context, snackbarHostState, onNavigate) }
    }

    OrdersContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * The Orders tab without a ViewModel: "My Orders", Active/Past tabs with counts, then a skeleton
 * while loading, the tab's empty state, or its order cards.
 */
@Composable
fun OrdersContent(
    state: OrdersState,
    onIntent: (OrdersIntent) -> Unit,
    onNavigate: (OrdersDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(title = stringResource(R.string.orders_title), centered = false)
        SegmentedTabs(
            tabs = listOf(
                SegmentedTab(stringResource(R.string.orders_tab_active), state.activeCount.takeUnless { state.loading }),
                SegmentedTab(stringResource(R.string.orders_tab_past), state.pastCount.takeUnless { state.loading }),
            ),
            selectedIndex = state.tab.ordinal,
            onSelect = { onIntent(OrdersIntent.SelectTab(OrdersTab.entries[it])) },
            modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 4.dp, bottom = 16.dp),
        )
        when {
            state.loading -> OrdersSkeleton()
            state.orders.isEmpty() -> {
                val active = state.tab == OrdersTab.ACTIVE
                StateMessage(
                    icon = if (active) ShoplyAppIcons.Receipt else ShoplyAppIcons.Package,
                    title = stringResource(if (active) R.string.orders_empty_active_title else R.string.orders_empty_past_title),
                    message = stringResource(if (active) R.string.orders_empty_active_message else R.string.orders_empty_past_message),
                    actionLabel = stringResource(R.string.cart_empty_action),
                    onAction = { onNavigate(OrdersDestination.StartShopping) },
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, bottom = ShoplyDimens.gutter),
                verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            ) {
                items(state.orders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onClick = { onNavigate(OrdersDestination.OrderDetails(order.id)) },
                        onReorder = { onIntent(OrdersIntent.Reorder(order.id)) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

/** Shared by both screens: a snackbar for messages, the cart after a reorder. */
internal fun CoroutineScope.handleOrdersEvent(
    event: OrdersEvent,
    context: Context,
    snackbarHostState: SnackbarHostState,
    onNavigate: (OrdersDestination) -> Unit,
) {
    when (event) {
        is OrdersEvent.ShowMessage -> launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(context.getString(event.message.textRes()), duration = SnackbarDuration.Short)
        }
        OrdersEvent.OpenCart -> onNavigate(OrdersDestination.Cart)
    }
}

private fun OrdersMessage.textRes(): Int = when (this) {
    OrdersMessage.REORDERED -> R.string.snackbar_reordered
    OrdersMessage.REORDER_FAILED -> R.string.snackbar_reorder_failed
}

@Composable
private fun OrdersSkeleton() {
    SkeletonPulse(modifier = Modifier.padding(horizontal = ShoplyDimens.gutter)) {
        Column(verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3)) {
            repeat(3) {
                Column(modifier = Modifier.fillMaxWidth().shoplyCard().padding(16.dp)) {
                    SkeletonBone(modifier = Modifier.fillMaxWidth(0.4f).height(15.dp))
                    SkeletonBone(modifier = Modifier.padding(top = 8.dp).fillMaxWidth(0.3f).height(12.dp))
                    Row(modifier = Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(2) { SkeletonBone(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(12.dp)) }
                    }
                    SkeletonBone(modifier = Modifier.padding(top = 14.dp).fillMaxWidth(0.5f).height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun OrdersContentPreview(state: OrdersState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            OrdersContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

private val previewActive = OrdersPreviewData.all.filter { it.status.isActive() }
private val previewPast = OrdersPreviewData.all.filterNot { it.status.isActive() }

@PreviewLightDark
@Composable
fun OrdersActivePreview() = OrdersContentPreview(
    OrdersState(loading = false, orders = previewActive, activeCount = previewActive.size, pastCount = previewPast.size),
)

@PreviewLightDark
@Composable
fun OrdersPastPreview() = OrdersContentPreview(
    OrdersState(tab = OrdersTab.PAST, loading = false, orders = previewPast, activeCount = previewActive.size, pastCount = previewPast.size),
)

@PreviewLightDark
@Composable
fun OrdersEmptyPreview() = OrdersContentPreview(OrdersState(loading = false))

@PreviewLightDark
@Composable
fun OrdersLoadingPreview() = OrdersContentPreview(OrdersState())
