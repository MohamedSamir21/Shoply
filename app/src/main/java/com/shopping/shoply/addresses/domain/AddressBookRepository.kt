package com.shopping.shoply.addresses.domain

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.model.DeliveryAddress

/**
 * Writes to the signed-in user's saved addresses (reads come from
 * [com.shopping.shoply.profile.domain.ProfileRepository.observeAddresses]). Writes are local-first:
 * they show at once and sync when online.
 */
interface AddressBookRepository {

    /**
     * Creates [address] (blank id) or replaces it, and sets `primary = false` on [demoteIds] in the
     * same write, so there is only ever one default.
     *
     * @return the address id.
     */
    suspend fun save(address: DeliveryAddress, demoteIds: List<String>): Result<String, DataError>

    /** Deletes [id], and makes [promoteId] the default in the same write when not null. */
    suspend fun delete(id: String, promoteId: String?): Result<Unit, DataError>
}
