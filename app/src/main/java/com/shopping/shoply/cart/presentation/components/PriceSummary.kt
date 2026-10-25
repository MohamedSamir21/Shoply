package com.shopping.shoply.cart.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.cart.domain.rules.CartTotals
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.common.presentation.util.formatPrice

/**
 * The price card: Subtotal, Delivery ("Free" in green), "Promo (CODE)" when [promoCode] is set and
 * there's a discount, a divider, then Total.
 */
@Composable
fun PriceSummary(totals: CartTotals, promoCode: String?, modifier: Modifier = Modifier) {
    val green = MaterialTheme.shoplyExtra.onGreenSoft
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shoplyCard()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
    ) {
        SummaryRow(stringResource(R.string.cart_subtotal), formatPrice(totals.subtotalCents))
        if (totals.deliveryCents == 0L) {
            SummaryRow(stringResource(R.string.cart_delivery), stringResource(R.string.cart_delivery_free), green)
        } else {
            SummaryRow(stringResource(R.string.cart_delivery), formatPrice(totals.deliveryCents))
        }
        if (promoCode != null && totals.discountCents > 0) {
            SummaryRow(
                label = stringResource(R.string.cart_promo_row, promoCode),
                value = "−" + formatPrice(totals.discountCents),
                valueColor = green,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(R.string.cart_total),
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatPrice(totals.totalCents),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row {
        Text(
            text = label,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

/** Over $35 with SHOPLY10 (free delivery, discount row). */
@PreviewLightDark
@Composable
fun PriceSummaryPromoPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PriceSummary(
                totals = CartTotals(
                    itemCount = 4,
                    subtotalCents = 33700,
                    deliveryCents = 0,
                    discountCents = 3370,
                    totalCents = 30330,
                ),
                promoCode = "SHOPLY10",
                modifier = Modifier.padding(ShoplyDimens.gutter),
            )
        }
    }
}

/** Under $35, no promo (delivery charged). */
@PreviewLightDark
@Composable
fun PriceSummaryDeliveryPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            PriceSummary(
                totals = CartTotals(itemCount = 1, subtotalCents = 1600, deliveryCents = 199, totalCents = 1799),
                promoCode = null,
                modifier = Modifier.padding(ShoplyDimens.gutter),
            )
        }
    }
}
