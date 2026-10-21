package com.shopping.shoply.common.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.cart.domain.rules.cartBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Backs the bottom-navigation shell (Home, Categories, Cart, Orders, Profile).
 *
 * [cartBadge] is the cart tab's badge text from [com.shopping.shoply.cart.domain.rules.cartBadge]:
 * null hides it. It updates live as the cart changes from any screen.
 */
@HiltViewModel
class MainShellViewModel @Inject constructor(
    cartRepository: CartRepository,
) : ViewModel() {

    val cartBadge: StateFlow<String?> = cartRepository.observeTotalQuantity()
        .map(::cartBadge)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
