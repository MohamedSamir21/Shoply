package com.shopping.shoply.payments.domain

import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

/**
 * Writes to the saved cards (reads come from [com.shopping.shoply.checkout.domain.PaymentRepository]).
 * Only brand, last 4 digits and expiry are ever stored: never the full number or the CVV.
 * Each write is one local-first batch, so there's always exactly one default card.
 */
interface CardWalletRepository {

    /** Saves a new [card] (its id is ignored) and clears `primary` on [demoteIds]. Returns the new id. */
    suspend fun add(card: PaymentCard, demoteIds: List<String>): Result<String, DataError>

    /** Makes [id] the default and clears `primary` on [demoteIds]. */
    suspend fun setDefault(id: String, demoteIds: List<String>): Result<Unit, DataError>

    /** Removes [id], and makes [promoteId] the default when not null. */
    suspend fun remove(id: String, promoteId: String?): Result<Unit, DataError>
}
