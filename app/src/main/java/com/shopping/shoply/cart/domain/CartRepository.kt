package com.shopping.shoply.cart.domain

import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Variant
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.Flow

/** The signed-in user's cart. */
interface CartRepository {

    /**
     * Sum of the quantities of all cart items (2 mugs + 1 shirt = 3). Emits right away, then on
     * every change. Never completes and never throws: when offline it keeps the last known value.
     */
    fun observeTotalQuantity(): Flow<Int>

    /**
     * Adds [quantity] of [product] in [variant]. Adding the same product and variant again increases
     * that item's quantity instead of creating a second item.
     *
     * Takes the whole [Product] because the cart item keeps a copy of its name and price, so the
     * cart doesn't need to read the catalog again.
     */
    suspend fun add(product: Product, variant: Variant, quantity: Int = 1): Result<Unit, DataError>

    /**
     * The cart's items, oldest added first. Emits right away, then on every change. Never completes
     * and never throws: when offline it keeps the last known list.
     */
    fun observeItems(): Flow<List<CartItem>>

    /** Sets the quantity of the item [itemId] (a [CartItem.id]). */
    suspend fun setQuantity(itemId: String, quantity: Int): Result<Unit, DataError>

    /** Removes the item [itemId] (a [CartItem.id]). */
    suspend fun remove(itemId: String): Result<Unit, DataError>
}
