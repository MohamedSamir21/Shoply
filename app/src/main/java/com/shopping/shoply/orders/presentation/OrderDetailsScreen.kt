package com.shopping.shoply.orders.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.cart.presentation.components.PriceSummary
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.darkRing
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.util.formatPrice
import com.shopping.shoply.home.presentation.components.ProductImage
import com.shopping.shoply.orders.domain.model.Order
import com.shopping.shoply.orders.domain.model.OrderPayment
import com.shopping.shoply.orders.domain.model.OrderStatus
import com.shopping.shoply.orders.domain.rules.isActive
import com.shopping.shoply.orders.presentation.components.OrderStatusTimeline
import com.shopping.shoply.orders.presentation.components.StatusChip
import com.shopping.shoply.orders.presentation.components.arrivalLabel
import com.shopping.shoply.orders.presentation.components.dateTimeLabel
import com.shopping.shoply.orders.presentation.components.labelRes
import com.shopping.shoply.orders.presentation.components.paymentLabel
import com.shopping.shoply.orders.presentation.preview.OrdersPreviewData

/** Order Details. Snackbars go to [snackbarHostState]. */
@Composable
fun OrderDetailsScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (OrdersDestination) -> Unit,
    viewModel: OrderDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event -> handleOrdersEvent(event, context, snackbarHostState, onNavigate) }
    }

    OrderDetailsContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * Order Details without a ViewModel: "Order #id", a status card (headline, chip, timeline or the
 * cancelled note), the items (each opens its product), delivery & payment, the summary, and
 * Reorder pinned at the bottom once the order is delivered.
 */
@Composable
fun OrderDetailsContent(
    state: OrderDetailsState,
    onIntent: (OrderDetailsIntent) -> Unit,
    onNavigate: (OrdersDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val body = state.body
    val order = (body as? OrderDetailsBody.Content)?.order
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(
            title = order?.let { stringResource(R.string.orders_number, it.id) } ?: stringResource(R.string.screen_order_details),
            onBack = { onNavigate(OrdersDestination.Back) },
        )
        when (body) {
            OrderDetailsBody.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            OrderDetailsBody.NotFound -> StateMessage(
                icon = ShoplyAppIcons.Receipt,
                title = stringResource(R.string.orders_not_found_title),
                message = stringResource(R.string.orders_not_found_message),
                actionLabel = stringResource(R.string.orders_not_found_action),
                onAction = { onNavigate(OrdersDestination.Back) },
            )
            is OrderDetailsBody.Content -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 4.dp, bottom = ShoplyDimens.gutter),
                ) {
                    StatusCard(body.order)
                    OrderItems(body.order, onItemClick = { onNavigate(OrdersDestination.Product(it)) })
                    DeliveryAndPayment(body.order)
                    SectionLabel(stringResource(R.string.orders_summary_title))
                    PriceSummary(totals = body.order.totals, promoCode = body.order.promoCode)
                }
                if (body.order.status == OrderStatus.DELIVERED) {
                    Surface(color = MaterialTheme.colorScheme.surface) {
                        Column {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            ShoplyButton(
                                label = stringResource(R.string.orders_reorder),
                                onClick = { onIntent(OrderDetailsIntent.Reorder) },
                                enable = !state.reordering,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = ShoplyDimens.gutter, vertical = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(order: Order) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth().shoplyCard().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.orders_placed_on, dateTimeLabel(order.createdAtMillis)),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                )
                Text(
                    text = when {
                        order.status.isActive() -> arrivalLabel(order)
                        order.status == OrderStatus.CANCELLED -> stringResource(R.string.orders_headline_cancelled)
                        else -> stringResource(order.status.labelRes())
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            StatusChip(order.status)
        }
        if (order.status == OrderStatus.CANCELLED) {
            Text(
                text = stringResource(R.string.orders_cancelled_message, paymentLabel(order.payment)),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 21.sp,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        } else {
            OrderStatusTimeline(order, modifier = Modifier.padding(top = 18.dp))
        }
    }
}

@Composable
private fun OrderItems(order: Order, onItemClick: (String) -> Unit) {
    SectionLabel(stringResource(R.string.orders_items_title, order.items.size))
    Column(modifier = Modifier.fillMaxWidth().shoplyCard().padding(horizontal = 16.dp, vertical = 4.dp)) {
        order.items.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onItemClick(item.productId) }.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProductImage(icon = item.icon, modifier = Modifier.size(52.dp), cornerRadius = 12.dp, circleRatio = 0.72f)
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = listOfNotNull(item.color, item.size, "× ${item.quantity}").joinToString(" · "),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text(
                    text = formatPrice(item.priceCents * item.quantity),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun DeliveryAndPayment(order: Order) {
    SectionLabel(stringResource(R.string.orders_delivery_payment_title))
    Column(
        modifier = Modifier.fillMaxWidth().shoplyCard().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        val address = order.address
        InfoRow(ShoplyAppIcons.Pin, address.label, listOf(address.line1, address.line2).filter { it.isNotBlank() }.joinToString(", "))
        InfoRow(
            icon = when (order.payment) {
                is OrderPayment.Card -> ShoplyAppIcons.Card
                is OrderPayment.Installments -> ShoplyAppIcons.Split
                OrderPayment.CashOnDelivery -> ShoplyAppIcons.Cash
            },
            label = stringResource(R.string.orders_payment),
            value = paymentLabel(order.payment),
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    val scheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(scheme.primaryContainer, RoundedCornerShape(10.dp))
                .darkRing(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = scheme.onSurfaceVariant)
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp,
                color = scheme.onSurface,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 15.5.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
    )
}

@Composable
private fun OrderDetailsPreview(state: OrderDetailsState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            OrderDetailsContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun OrderDetailsOnTheWayPreview() = OrderDetailsPreview(OrderDetailsState(OrderDetailsBody.Content(OrdersPreviewData.onTheWay)))

@PreviewLightDark
@Composable
fun OrderDetailsDeliveredPreview() = OrderDetailsPreview(OrderDetailsState(OrderDetailsBody.Content(OrdersPreviewData.delivered)))

@PreviewLightDark
@Composable
fun OrderDetailsCancelledPreview() = OrderDetailsPreview(OrderDetailsState(OrderDetailsBody.Content(OrdersPreviewData.cancelled)))

@PreviewLightDark
@Composable
fun OrderDetailsNotFoundPreview() = OrderDetailsPreview(OrderDetailsState(OrderDetailsBody.NotFound))
