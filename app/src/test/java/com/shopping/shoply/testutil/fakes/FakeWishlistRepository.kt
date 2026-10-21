package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.wishlist.domain.WishlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Like the real repository, a successful [add]/[remove] also updates [ids], so [observeIds] emits.
 * [gate] keeps a save pending (checks the instant heart flip and the ignored second tap).
 */
class FakeWishlistRepository(
    var result: Result<Unit, DataError> = Result.Success(Unit),
) : WishlistRepository {
    val ids = MutableStateFlow<Set<String>>(emptySet())
    val gate = Gate()
    val addCalls = mutableListOf<String>()
    val removeCalls = mutableListOf<String>()

    override fun observeIds() = ids

    override suspend fun add(productId: String): Result<Unit, DataError> {
        addCalls += productId
        gate.pass()
        if (result is Result.Success) ids.update { it + productId }
        return result
    }

    override suspend fun remove(productId: String): Result<Unit, DataError> {
        removeCalls += productId
        gate.pass()
        if (result is Result.Success) ids.update { it - productId }
        return result
    }
}
