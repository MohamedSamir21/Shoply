package com.shopping.shoply.checkout.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.checkout.presentation.components.AddressStep
import com.shopping.shoply.checkout.presentation.components.CheckoutStepper
import com.shopping.shoply.checkout.presentation.components.DeliveryStep
import com.shopping.shoply.checkout.presentation.components.PaymentStep
import com.shopping.shoply.checkout.presentation.components.ReviewStep
import com.shopping.shoply.checkout.presentation.preview.CheckoutPreviewData
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.util.formatPrice
import kotlinx.coroutines.launch

/** The 4-step Checkout. System back steps back like the top bar's ←. */
@Composable
fun CheckoutScreen(
    snackbarHostState: SnackbarHostState,
    onNavigate: (CheckoutDestination) -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler { viewModel.onIntent(CheckoutIntent.Back) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckoutEvent.ShowMessage -> launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(event.message.textRes()),
                        duration = SnackbarDuration.Short,
                    )
                }
                CheckoutEvent.Back -> onNavigate(CheckoutDestination.Back)
                is CheckoutEvent.OrderPlaced -> onNavigate(CheckoutDestination.OrderPlaced(event.orderId))
            }
        }
    }

    CheckoutContent(state = state, onIntent = viewModel::onIntent, onNavigate = onNavigate)
}

/**
 * Checkout without a ViewModel (previews and UI tests use this): top bar, [CheckoutStepper], the
 * current step's body, and a pinned bar with Continue (or Total + Place Order on Review).
 * A spinner shows until the cart arrives; an empty cart (not mid-order) shows "Your Cart Is Empty".
 */
@Composable
fun CheckoutContent(
    state: CheckoutState,
    onIntent: (CheckoutIntent) -> Unit,
    onNavigate: (CheckoutDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(title = stringResource(R.string.screen_checkout), onBack = { onIntent(CheckoutIntent.Back) })
        when {
            state.loading -> Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.items.isEmpty() && !state.placing -> StateMessage(
                icon = ShoplyAppIcons.Bag,
                title = stringResource(R.string.cart_empty_title),
                message = stringResource(R.string.checkout_empty_message),
                actionLabel = stringResource(R.string.cart_empty_action),
                onAction = { onNavigate(CheckoutDestination.Back) },
            )
            else -> {
                CheckoutStepper(step = state.step, onStepClick = { onIntent(CheckoutIntent.GoToStep(it)) })
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 6.dp, bottom = ShoplyDimens.gutter),
                ) {
                    when (state.step) {
                        STEP_ADDRESS -> AddressStep(state, onIntent, onAddAddress = { onNavigate(CheckoutDestination.AddAddress) })
                        STEP_DELIVERY -> DeliveryStep(state, onIntent)
                        STEP_PAYMENT -> PaymentStep(state, onIntent, onAddCard = { onNavigate(CheckoutDestination.AddCard) })
                        else -> ReviewStep(state, onIntent)
                    }
                }
                CheckoutBar(state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun CheckoutBar(state: CheckoutState, onIntent: (CheckoutIntent) -> Unit) {
    val review = state.step == STEP_REVIEW
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (review) {
                    Column {
                        Text(
                            text = stringResource(R.string.cart_total),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = formatPrice(state.totals.totalCents),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                ShoplyButton(
                    label = stringResource(if (review) R.string.checkout_place_order else R.string.checkout_continue),
                    onClick = { onIntent(if (review) CheckoutIntent.PlaceOrder else CheckoutIntent.Continue) },
                    enable = if (review) !state.placing else state.canContinue,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun CheckoutMessage.textRes(): Int = when (this) {
    CheckoutMessage.ORDER_FAILED -> R.string.snackbar_order_failed
}

@Composable
private fun CheckoutContentPreview(state: CheckoutState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CheckoutContent(state = state, onIntent = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun CheckoutAddressPreview() = CheckoutContentPreview(CheckoutPreviewData.state)

@PreviewLightDark
@Composable
fun CheckoutDeliveryPreview() = CheckoutContentPreview(CheckoutPreviewData.state.copy(step = STEP_DELIVERY))

@PreviewLightDark
@Composable
fun CheckoutPaymentPreview() = CheckoutContentPreview(CheckoutPreviewData.installments)

@PreviewLightDark
@Composable
fun CheckoutReviewPreview() = CheckoutContentPreview(CheckoutPreviewData.state.copy(step = STEP_REVIEW))

@PreviewLightDark
@Composable
fun CheckoutLoadingPreview() = CheckoutContentPreview(CheckoutState())
