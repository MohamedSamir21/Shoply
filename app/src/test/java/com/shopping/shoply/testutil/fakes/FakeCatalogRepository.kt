package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.categories

/**
 * [getCategoriesCalls] counts loads (checks "Retry reloads").
 * [gate] keeps a load pending (checks the Loading state).
 */
class FakeCatalogRepository(
    var result: Result<List<Category>, DataError> = Result.Success(categories(3)),
) : CatalogRepository {
    val gate = Gate()
    var getCategoriesCalls = 0
        private set

    override suspend fun getCategories(): Result<List<Category>, DataError> {
        getCategoriesCalls++
        gate.pass()
        return result
    }
}
