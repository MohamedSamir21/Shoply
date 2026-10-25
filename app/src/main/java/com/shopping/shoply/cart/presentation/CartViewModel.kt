package com.shopping.shoply.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.cart.domain.PromoCodeRepository
import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.cart.domain.rules.MAX_QUANTITY
import com.shopping.shoply.cart.domain.rules.cartTotals
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.common.domain.Result
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

/**
 * Backs the Cart screen: the live cart items and their totals.
 *
 * - Each store's delivery fee is read once, the first time one of its items shows up; until then
 *   (or if the read fails) it counts as free.
 * - Quantity changes stay within 1..20; removing is its own action.
 * - The applied promo code lives here for now (Checkout will read it later).
 *
 * [state] starts eagerly, so its value is always current, even before anything collects it.
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val promoCodeRepository: PromoCodeRepository,
    private val catalogRepository: CatalogRepository,
) : ViewModel() {

    private val storeFees = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val requestedStores = mutableSetOf<String>()
    private val promo = MutableStateFlow<AppliedPromo?>(null)
    private val promoForm = MutableStateFlow(PromoForm())

    // null until the first list arrives.
    private val items: StateFlow<List<CartItem>?> = cartRepository.observeItems()
        .onEach(::loadMissingFees)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state: StateFlow<CartState> = combine(items, storeFees, promo, promoForm) { items, fees, promo, form ->
        CartState(
            body = when {
                items == null -> CartBody.Loading
                items.isEmpty() -> CartBody.Empty
                else -> CartBody.Content(items)
            },
            totals = cartTotals(items.orEmpty(), fees, promo?.percentOff),
            promo = promo,
            promoInput = form.input,
            promoInvalid = form.invalid,
            applyingPromo = form.applying,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CartState())

    private val _events = Channel<CartEvent>(Channel.BUFFERED)
    val events: Flow<CartEvent> = _events.receiveAsFlow()

    fun onIntent(intent: CartIntent) {
        when (intent) {
            is CartIntent.Increase -> changeQuantity(intent.itemId, delta = 1)
            is CartIntent.Decrease -> changeQuantity(intent.itemId, delta = -1)
            is CartIntent.Remove -> remove(intent.itemId)
            is CartIntent.PromoInputChanged -> promoForm.update { it.copy(input = intent.text, invalid = false) }
            CartIntent.ApplyPromo -> applyPromo()
            CartIntent.RemovePromo -> promo.value = null
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

    private fun changeQuantity(itemId: String, delta: Int) {
        val item = items.value?.firstOrNull { it.id == itemId } ?: return
        val quantity = item.quantity + delta
        if (quantity !in 1..MAX_QUANTITY) return
        viewModelScope.launch {
            if (cartRepository.setQuantity(itemId, quantity) is Result.Error) {
                _events.send(CartEvent.ShowMessage(CartMessage.UPDATE_FAILED))
            }
        }
    }

    private fun remove(itemId: String) {
        viewModelScope.launch {
            val message = when (cartRepository.remove(itemId)) {
                is Result.Success -> CartMessage.REMOVED
                is Result.Error -> CartMessage.UPDATE_FAILED
            }
            _events.send(CartEvent.ShowMessage(message))
        }
    }

    private fun applyPromo() {
        val form = promoForm.value
        val code = form.input.trim().uppercase()
        if (code.isEmpty() || form.applying) return
        promoForm.update { it.copy(applying = true) }
        viewModelScope.launch {
            when (val result = promoCodeRepository.percentOff(code)) {
                is Result.Success -> {
                    val percentOff = result.data
                    if (percentOff == null) {
                        promoForm.update { it.copy(invalid = true, applying = false) }
                    } else {
                        promo.value = AppliedPromo(code, percentOff)
                        promoForm.value = PromoForm()
                    }
                }
                is Result.Error -> {
                    promoForm.update { it.copy(applying = false) }
                    _events.send(CartEvent.ShowMessage(CartMessage.PROMO_FAILED))
                }
            }
        }
    }

    private data class PromoForm(val input: String = "", val invalid: Boolean = false, val applying: Boolean = false)
}
