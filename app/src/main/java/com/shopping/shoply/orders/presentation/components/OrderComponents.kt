package com.shopping.shoply.orders.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.common.presentation.util.formatPrice
import com.shopping.shoply.home.presentation.components.ProductImage
import com.shopping.shoply.orders.domain.model.Order
import com.shopping.shoply.orders.domain.model.OrderStatus
import com.shopping.shoply.orders.domain.rules.isActive
import com.shopping.shoply.orders.presentation.preview.OrdersPreviewData

/** The status pill: orange while active, green when delivered, red when cancelled. */
@Composable
fun StatusChip(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val extra = MaterialTheme.shoplyExtra
    val scheme = MaterialTheme.colorScheme
    val (container, content) = when (status) {
        OrderStatus.DELIVERED -> extra.greenSoft to extra.onGreenSoft
        OrderStatus.CANCELLED -> extra.redSoft to extra.red
        else -> scheme.primaryContainer to scheme.onPrimaryContainer
    }
    Text(
        text = stringResource(status.labelRes()),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = content,
        modifier = modifier
            .background(container, RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/**
 * An order in the list: number, date and status; up to 3 product images (+N); the arrival line
 * while active; then "N items · total" and one action (Track Order / Reorder / Details).
 */
@Composable
fun OrderCard(
    order: Order,
    onClick: () -> Unit,
    onReorder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val active = order.status.isActive()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shoplyCard()
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.orders_number, order.id),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = dateTimeLabel(order.createdAtMillis),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            StatusChip(order.status)
        }
        OrderThumbs(order, modifier = Modifier.padding(top = 14.dp))
        if (active) {
            Row(
                modifier = Modifier.padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    ShoplyAppIcons.Clock,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    arrivalLabel(order),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
            }
        }
        HorizontalDivider(
            color = scheme.outlineVariant,
            modifier = Modifier.padding(top = 14.dp, bottom = 12.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            val count = order.totals.itemCount
            Text(
                text = buildAnnotatedString {
                    append(pluralStringResource(R.plurals.orders_item_count, count, count) + " · ")
                    withStyle(
                        SpanStyle(
                            color = scheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    ) { append(formatPrice(order.totals.totalCents)) }
                },
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            when {
                active -> SmallActionButton(
                    stringResource(R.string.orders_track),
                    OrderActionStyle.Soft,
                    onClick
                )

                order.status == OrderStatus.DELIVERED -> SmallActionButton(
                    stringResource(R.string.orders_reorder),
                    OrderActionStyle.Outline,
                    onReorder
                )

                else -> SmallActionButton(
                    stringResource(R.string.orders_details),
                    OrderActionStyle.Ghost,
                    onClick
                )
            }
        }
    }
}

@Composable
private fun OrderThumbs(order: Order, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        order.items.take(3).forEach { item ->
            ProductImage(
                icon = item.icon,
                modifier = Modifier.size(52.dp),
                cornerRadius = 12.dp,
                circleRatio = 0.72f
            )
        }
        if (order.items.size > 3) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "+${order.items.size - 3}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

enum class OrderActionStyle { Soft, Outline, Ghost }

/** The design's 36 dp `SmallBtn`. */
@Composable
fun SmallActionButton(
    label: String,
    style: OrderActionStyle,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = if (style == OrderActionStyle.Soft) scheme.primaryContainer else Color.Transparent,
        contentColor = if (style == OrderActionStyle.Outline) scheme.onSurface else scheme.primary,
        border = if (style == OrderActionStyle.Outline) BorderStroke(
            1.4.dp,
            scheme.outline
        ) else null,
    ) {
        Box(
            modifier = Modifier
                .height(36.dp)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private val TIMELINE_STEPS = listOf(
    OrderStatus.PLACED,
    OrderStatus.CONFIRMED,
    OrderStatus.PACKED,
    OrderStatus.ON_THE_WAY,
    OrderStatus.DELIVERED,
)

/**
 * The 5 steps from placed to delivered. Reached steps get ✓ and the time from the order's
 * timeline; the current one is ringed; later ones are faint.
 */
@Composable
fun OrderStatusTimeline(order: Order, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val current = TIMELINE_STEPS.indexOf(order.status).coerceAtLeast(0)
    Column(modifier = modifier) {
        TIMELINE_STEPS.forEachIndexed { index, step ->
            val reached = index <= current
            val done = index < current || order.status == OrderStatus.DELIVERED
            val entry = order.timeline.lastOrNull { it.status == step }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(
                                if (done) scheme.primary else if (reached) scheme.primaryContainer else Color.Transparent,
                                CircleShape
                            )
                            .border(
                                2.dp,
                                if (reached) scheme.primary else scheme.outline,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (done) Icon(
                            ShoplyAppIcons.Check,
                            contentDescription = null,
                            tint = scheme.onPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    if (index < TIMELINE_STEPS.lastIndex) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(26.dp)
                                .background(if (index < current) scheme.primary else scheme.outlineVariant),
                        )
                    }
                }
                Column(modifier = Modifier
                    .weight(1f)
                    .padding(top = 1.dp)) {
                    Text(
                        text = stringResource(if (step == OrderStatus.PLACED) R.string.orders_step_placed else step.labelRes()),
                        fontSize = 14.sp,
                        fontWeight = if (index == current) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (reached) scheme.onSurface else MaterialTheme.shoplyExtra.inkFaint,
                    )
                    val detail = listOfNotNull(
                        entry?.let { timeLabel(it.atMillis) },
                        entry?.note
                    ).joinToString(" · ")
                    if (detail.isNotEmpty()) {
                        Text(
                            detail,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun OrderCardPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(ShoplyDimens.gutter),
                verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            ) {
                OrderCard(OrdersPreviewData.onTheWay, onClick = {}, onReorder = {})
                OrderCard(OrdersPreviewData.delivered, onClick = {}, onReorder = {})
                OrderCard(OrdersPreviewData.cancelled, onClick = {}, onReorder = {})
            }
        }
    }
}

@PreviewLightDark
@Composable
fun OrderStatusTimelinePreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            OrderStatusTimeline(
                OrdersPreviewData.onTheWay,
                modifier = Modifier.padding(ShoplyDimens.gutter)
            )
        }
    }
}
