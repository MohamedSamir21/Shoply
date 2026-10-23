package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.categories

/**
 * [getCategoriesCalls] counts loads (checks "Retry reloads").
 * [gate] keeps a load pending (checks the Loading state).
 *
 * [getProducts] answers from [products] (unknown ids are left out, like the real one), or fails
 * with [productsError] when set. [getProductsCalls] records the ids of each call.
 */
class FakeCatalogRepository(
    var result: Result<List<Category>, DataError> = Result.Success(categories(3)),
) : CatalogRepository {
    val gate = Gate()
    var getCategoriesCalls = 0
        private set

    var products: List<Product> = emptyList()
    var productsError: DataError? = null
    val getProductsCalls = mutableListOf<Set<String>>()

    override suspend fun getCategories(): Result<List<Category>, DataError> {
        getCategoriesCalls++
        gate.pass()
        return result
    }

    override suspend fun getProducts(ids: Set<String>): Result<List<Product>, DataError> {
        getProductsCalls += ids
        gate.pass()
        productsError?.let { return Result.Error(it) }
        return Result.Success(products.filter { it.id in ids })
    }
}
