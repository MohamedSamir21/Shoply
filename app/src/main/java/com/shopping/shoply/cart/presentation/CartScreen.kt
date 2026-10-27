package com.shopping.shoply.cart.presentation

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.cart.domain.rules.CartTotals
import com.shopping.shoply.cart.domain.rules.cartTotals
import com.shopping.shoply.cart.presentation.components.CartItemRow
import com.shopping.shoply.cart.presentation.components.PriceSummary
import com.shopping.shoply.cart.presentation.components.PromoCodeField
import com.shopping.shoply.cart.presentation.preview.CartPreviewData
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.components.TopBarIconButton
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.common.presentation.util.formatPrice
import kotlinx.coroutines.launch

/**
 * The Cart tab. Snackbars go to [snackbarHostState] (from MainShell, so they sit above the bottom
 * navigation).
 */
@Composable
fun CartScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (CartDestination) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CartEvent.ShowMessage -> launch {
                    // A new message replaces the one on screen instead of waiting behind it.
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(event.message.textRes()),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    CartContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * The Cart without a ViewModel (previews and UI tests use this): "My Cart" ("N items" and ♡ →
 * [CartDestination.Wishlist] while there's content), then the body by [CartState.body]:
 * - Loading → list skeleton
 * - Empty → "Your Cart Is Empty" + Start Shopping → [CartDestination.StartShopping]
 * - Content → item rows, swipe hint, promo code, price summary, and a pinned Total + Checkout bar.
 */
@Composable
fun CartContent(
    state: CartState,
    onIntent: (CartIntent) -> Unit,
    onNavigate: (CartDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val body = state.body
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(
            title = stringResource(R.string.screen_cart),
            centered = false,
            subtitle = if (body is CartBody.Content) {
                pluralStringResource(R.plurals.cart_item_count, state.totals.itemCount, state.totals.itemCount)
            } else {
                null
            },
        ) {
            if (body is CartBody.Content) {
                TopBarIconButton(
                    icon = ShoplyAppIcons.Heart,
                    label = stringResource(R.string.screen_wishlist),
                    onClick = { onNavigate(CartDestination.Wishlist) },
                )
            }
        }
        when (body) {
            CartBody.Loading -> CartSkeleton()
            CartBody.Empty -> StateMessage(
                icon = ShoplyAppIcons.Bag,
                title = stringResource(R.string.cart_empty_title),
                message = stringResource(R.string.cart_empty_message),
                actionLabel = stringResource(R.string.cart_empty_action),
                onAction = { onNavigate(CartDestination.StartShopping) },
            )
            is CartBody.Content -> {
                CartList(
                    body = body,
                    state = state,
                    onIntent = onIntent,
                    onNavigate = onNavigate,
                    modifier = Modifier.weight(1f),
                )
                CheckoutBar(totalCents = state.totals.totalCents, onCheckout = { onNavigate(CartDestination.Checkout(state.promo?.code)) })
            }
        }
    }
}

@Composable
private fun CartList(
    body: CartBody.Content,
    state: CartState,
    onIntent: (CartIntent) -> Unit,
    onNavigate: (CartDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = ShoplyDimens.gutter,
            end = ShoplyDimens.gutter,
            top = ShoplyDimens.space2,
            bottom = ShoplyDimens.space6,
        ),
        verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
    ) {
        items(body.items, key = { it.id }) { item ->
            CartItemRow(
                item = item,
                onClick = { onNavigate(CartDestination.Product(item.productId)) },
                onIncrease = { onIntent(CartIntent.Increase(item.id)) },
                onDecrease = { onIntent(CartIntent.Decrease(item.id)) },
                onRemove = { onIntent(CartIntent.Remove(item.id)) },
                modifier = Modifier.animateItem(),
            )
        }
        item(key = "hint") { SwipeHint() }
        item(key = "promo") {
            PromoCodeField(
                value = state.promoInput,
                onValueChange = { onIntent(CartIntent.PromoInputChanged(it)) },
                onApply = { onIntent(CartIntent.ApplyPromo) },
                onRemove = { onIntent(CartIntent.RemovePromo) },
                applied = state.promo,
                invalid = state.promoInvalid,
                applying = state.applyingPromo,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        item(key = "summary") {
            PriceSummary(
                totals = state.totals,
                promoCode = state.promo?.code,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

/** ⓘ "Swipe left on an item to remove it", centred and faint. */
@Composable
private fun SwipeHint() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val faint = MaterialTheme.shoplyExtra.inkFaint
        Icon(imageVector = ShoplyAppIcons.Info, contentDescription = null, tint = faint, modifier = Modifier.size(15.dp))
        Text(
            text = stringResource(R.string.cart_swipe_hint),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = faint,
        )
    }
}

/** Pinned above the bottom navigation: "Total" + amount, then Checkout. */
@Composable
private fun CheckoutBar(totalCents: Long, onCheckout: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.cart_total),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatPrice(totalCents),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                ShoplyButton(
                    label = stringResource(R.string.cart_checkout),
                    onClick = onCheckout,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Three row-shaped placeholders: image, two text lines, a price. */
@Composable
private fun CartSkeleton() {
    SkeletonPulse(
        modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = ShoplyDimens.space2),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space3)) {
            repeat(3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shoplyCard()
                        .padding(ShoplyDimens.space3),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    SkeletonBone(modifier = Modifier.size(72.dp), shape = RoundedCornerShape(12.dp))
                    Column(modifier = Modifier.padding(top = ShoplyDimens.space1)) {
                        SkeletonBone(modifier = Modifier.fillMaxWidth(0.8f).height(14.dp))
                        SkeletonBone(modifier = Modifier.padding(top = 10.dp).fillMaxWidth(0.5f).height(14.dp))
                        SkeletonBone(modifier = Modifier.padding(top = 14.dp).fillMaxWidth(0.34f).height(16.dp))
                    }
                }
            }
        }
    }
}

private fun CartMessage.textRes(): Int = when (this) {
    CartMessage.REMOVED -> R.string.snackbar_cart_removed
    CartMessage.UPDATE_FAILED -> R.string.snackbar_cart_update_failed
    CartMessage.PROMO_FAILED -> R.string.snackbar_promo_failed
}

@Composable
private fun CartContentPreview(state: CartState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CartContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun CartFilledPreview() = CartContentPreview(CartPreviewData.content)

/** Typed an unknown code. */
@PreviewLightDark
@Composable
fun CartInvalidPromoPreview() = CartContentPreview(
    CartPreviewData.content.copy(
        totals = cartTotals(CartPreviewData.items, emptyMap(), percentOff = null),
        promo = null,
        promoInput = "SAVE50",
        promoInvalid = true,
    ),
)

@PreviewLightDark
@Composable
fun CartEmptyPreview() = CartContentPreview(CartState(body = CartBody.Empty, totals = CartTotals()))

@PreviewLightDark
@Composable
fun CartLoadingPreview() = CartContentPreview(CartState())
