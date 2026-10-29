package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.addresses.domain.AddressBookRepository
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.model.DeliveryAddress

/**
 * Records each call. Like the real one, a successful write also updates [profile]'s addresses, so
 * screens observing them see the change. [error] makes every write fail.
 */
class FakeAddressBookRepository(private val profile: FakeProfileRepository) : AddressBookRepository {
    var error: DataError? = null
    val saveCalls = mutableListOf<Pair<DeliveryAddress, List<String>>>()
    val deleteCalls = mutableListOf<Pair<String, String?>>()

    override suspend fun save(address: DeliveryAddress, demoteIds: List<String>): Result<String, DataError> {
        saveCalls += address to demoteIds
        error?.let { return Result.Error(it) }
        val id = address.id.ifBlank { "new${saveCalls.size}" }
        profile.addresses.value = profile.addresses.value
            .filterNot { it.id == id }
            .map { if (it.id in demoteIds) it.copy(primary = false) else it } + address.copy(id = id)
        return Result.Success(id)
    }

    override suspend fun delete(id: String, promoteId: String?): Result<Unit, DataError> {
        deleteCalls += id to promoteId
        error?.let { return Result.Error(it) }
        profile.addresses.value = profile.addresses.value
            .filterNot { it.id == id }
            .map { if (it.id == promoteId) it.copy(primary = true) else it }
        return Result.Success(Unit)
    }
}
