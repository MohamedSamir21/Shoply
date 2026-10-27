package com.shopping.shoply.checkout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.components.ShoplyButtonVariant
import com.shopping.shoply.common.presentation.components.ResultBadge
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/** Nav argument holding the placed order's id. */
const val ORDER_ID_ARG = "orderId"

/**
 * Success after Place Order, in the auth Congratulations style: the green badge, "Order Placed!",
 * the order number, then Track Order (Orders tab) and Continue Shopping (Home).
 */
@Composable
fun OrderPlacedScreen(
    orderId: String,
    onTrackOrder: () -> Unit,
    onContinueShopping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ShoplyDimens.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ResultBadge(ok = true, size = 160.dp)
        Spacer(Modifier.height(ShoplyDimens.space5))
        Text(
            text = stringResource(R.string.order_placed_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.shoplyExtra.green,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ShoplyDimens.space2))
        Text(
            text = stringResource(R.string.order_placed_message, orderId),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(ShoplyDimens.space8))
        ShoplyButton(
            label = stringResource(R.string.order_placed_track),
            onClick = onTrackOrder,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(ShoplyDimens.space3))
        ShoplyButton(
            label = stringResource(R.string.order_placed_continue),
            onClick = onContinueShopping,
            variant = ShoplyButtonVariant.Outline,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@PreviewLightDark
@Composable
fun OrderPlacedScreenPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            OrderPlacedScreen(orderId = "SH-20481", onTrackOrder = {}, onContinueShopping = {})
        }
    }
}
