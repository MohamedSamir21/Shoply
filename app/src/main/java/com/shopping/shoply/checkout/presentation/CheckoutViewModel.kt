package com.shopping.shoply.checkout.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.cart.domain.PromoCodeRepository
import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.cart.domain.rules.EXPRESS_FEE_CENTS
import com.shopping.shoply.cart.domain.rules.cartTotals
import com.shopping.shoply.cart.presentation.AppliedPromo
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.checkout.domain.OrderRepository
import com.shopping.shoply.checkout.domain.PaymentRepository
import com.shopping.shoply.checkout.domain.model.DeliveryOption
import com.shopping.shoply.checkout.domain.model.OrderRequest
import com.shopping.shoply.checkout.domain.model.PaymentChoice
import com.shopping.shoply.checkout.domain.rules.DeliveryDay
import com.shopping.shoply.checkout.domain.rules.deliveryDays
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.NowProvider
import com.shopping.shoply.profile.domain.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Nav argument holding the promo code applied in the Cart (absent when none). */
const val PROMO_CODE_ARG = "promo"

/**
 * Backs the 4-step Checkout: address, delivery, payment, review.
 *
 * - The cart, addresses and cards are live. Defaults: the primary address, Standard delivery on the
 *   first free slot, and the primary card. A picked address that disappears falls back to the default.
 * - The promo code from the Cart is checked again; an unknown code or a failed check gives no discount.
 * - Continue moves on only when the step is complete; back steps back, or leaves from the first one.
 * - Place Order is ignored while it runs; on success it stays disabled until the screen leaves.
 *
 * [state] starts eagerly, so its value is always current, even before anything collects it.
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    cartRepository: CartRepository,
    private val promoCodeRepository: PromoCodeRepository,
    private val catalogRepository: CatalogRepository,
    profileRepository: ProfileRepository,
    paymentRepository: PaymentRepository,
    private val orderRepository: OrderRepository,
    nowProvider: NowProvider,
) : ViewModel() {

    private val days: List<DeliveryDay> = deliveryDays(nowProvider.nowMillis())
    private val storeFees = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val requestedStores = mutableSetOf<String>()
    private val promo = MutableStateFlow<AppliedPromo?>(null)
    private val choices = MutableStateFlow(Choices(slotHour = firstFreeSlot(0)))

    // null until the first list arrives.
    private val items: StateFlow<List<CartItem>?> = cartRepository.observeItems()
        .onEach(::loadMissingFees)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val savedDetails = combine(profileRepository.observeAddresses(), paymentRepository.observeCards(), ::Pair)

    val state: StateFlow<CheckoutState> =
        combine(items, storeFees, promo, savedDetails, choices) { items, fees, promo, (addresses, cards), picked ->
            val express = picked.speed == DeliverySpeed.EXPRESS
            CheckoutState(
                step = picked.step,
                loading = items == null,
                items = items.orEmpty(),
                addresses = addresses,
                selectedAddressId = picked.addressId?.takeIf { id -> addresses.any { it.id == id } }
                    ?: addresses.firstOrNull()?.id,
                speed = picked.speed,
                days = days,
                dayIndex = picked.dayIndex,
                slotHour = picked.slotHour,
                cards = cards,
                payment = picked.payment ?: cards.firstOrNull()?.let { PaymentChoice.Card(it) },
                totals = cartTotals(
                    items.orEmpty(),
                    fees,
                    promo?.percentOff,
                    expressFeeCents = if (express) EXPRESS_FEE_CENTS else 0,
                ),
                promo = promo,
                placing = picked.placing,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, CheckoutState(days = days))

    private val _events = Channel<CheckoutEvent>(Channel.BUFFERED)
    val events: Flow<CheckoutEvent> = _events.receiveAsFlow()

    init {
        savedStateHandle.get<String>(PROMO_CODE_ARG)?.takeIf { it.isNotBlank() }?.let(::checkPromo)
    }

    fun onIntent(intent: CheckoutIntent) {
        when (intent) {
            is CheckoutIntent.SelectAddress -> choices.update { it.copy(addressId = intent.id) }
            is CheckoutIntent.SelectSpeed -> choices.update { it.copy(speed = intent.speed) }
            is CheckoutIntent.SelectDay -> selectDay(intent.index)
            is CheckoutIntent.SelectSlot -> selectSlot(intent.startHour)
            is CheckoutIntent.SelectPayment -> choices.update { it.copy(payment = intent.choice) }
            is CheckoutIntent.GoToStep -> choices.update { if (intent.step in 0 until it.step) it.copy(step = intent.step) else it }
            CheckoutIntent.Continue -> if (state.value.canContinue) choices.update { it.copy(step = it.step + 1) }
            CheckoutIntent.Back -> back()
            CheckoutIntent.PlaceOrder -> placeOrder()
        }
    }

    private fun checkPromo(code: String) {
        viewModelScope.launch {
            val result = promoCodeRepository.percentOff(code)
            val percentOff = (result as? Result.Success)?.data ?: return@launch
            promo.value = AppliedPromo(code, percentOff)
        }
    }

    private fun loadMissingFees(items: List<CartItem>) {
        items.map { it.storeId }.distinct().filter { requestedStores.add(it) }.forEach { storeId ->
            viewModelScope.launch {
                val result = catalogRepository.getStore(storeId)
                if (result is Result.Success) storeFees.update { it + (storeId to result.data.deliveryFeeCents) }
            }
        }
    }

    private fun selectDay(index: Int) {
        if (index !in days.indices) return
        choices.update { it.copy(dayIndex = index, slotHour = firstFreeSlot(index)) }
    }

    private fun selectSlot(startHour: Int) {
        val slot = days[choices.value.dayIndex].slots.firstOrNull { it.startHour == startHour }
        if (slot == null || slot.full) return
        choices.update { it.copy(slotHour = startHour) }
    }

    private fun back() {
        if (choices.value.step == STEP_ADDRESS) {
            _events.trySend(CheckoutEvent.Back)
        } else {
            choices.update { it.copy(step = it.step - 1) }
        }
    }

    private fun placeOrder() {
        val current = state.value
        if (current.placing || current.items.isEmpty()) return
        val address = current.selectedAddress ?: return
        val payment = current.payment ?: return
        val delivery = when (current.speed) {
            DeliverySpeed.EXPRESS -> DeliveryOption.Express
            DeliverySpeed.STANDARD -> DeliveryOption.Standard(
                dayStartMillis = days[current.dayIndex].startMillis,
                slotStartHour = current.slotHour ?: return,
            )
        }
        choices.update { it.copy(placing = true) }
        viewModelScope.launch {
            val request = OrderRequest(current.items, address, delivery, payment, current.promo?.code, current.totals)
            when (val result = orderRepository.place(request)) {
                is Result.Success -> _events.send(CheckoutEvent.OrderPlaced(result.data))
                is Result.Error -> {
                    choices.update { it.copy(placing = false) }
                    _events.send(CheckoutEvent.ShowMessage(CheckoutMessage.ORDER_FAILED))
                }
            }
        }
    }

    private fun firstFreeSlot(dayIndex: Int): Int? = days.getOrNull(dayIndex)?.slots?.firstOrNull { !it.full }?.startHour

    /** What the user picked; null address/payment means "use the default". */
    private data class Choices(
        val step: Int = STEP_ADDRESS,
        val addressId: String? = null,
        val speed: DeliverySpeed = DeliverySpeed.STANDARD,
        val dayIndex: Int = 0,
        val slotHour: Int? = null,
        val payment: PaymentChoice? = null,
        val placing: Boolean = false,
    )
}
