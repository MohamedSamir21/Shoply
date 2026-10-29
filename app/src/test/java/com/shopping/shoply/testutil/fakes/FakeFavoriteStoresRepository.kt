package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.store.domain.FavoriteStoresRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Like the real repository, a successful [setFavorite] also updates [ids], so [observeIds] emits.
 * [gate] keeps a save pending (checks the instant heart flip).
 */
class FakeFavoriteStoresRepository(
    var result: Result<Unit, DataError> = Result.Success(Unit),
) : FavoriteStoresRepository {
    val ids = MutableStateFlow<Set<String>>(emptySet())
    val gate = Gate()

    override fun observeIds() = ids

    override suspend fun setFavorite(storeId: String, favorite: Boolean): Result<Unit, DataError> {
        gate.pass()
        if (result is Result.Success) ids.update { if (favorite) it + storeId else it - storeId }
        return result
    }
}
