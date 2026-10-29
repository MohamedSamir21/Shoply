package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.categories

/**
 * [getCategoriesCalls] counts loads (checks "Retry reloads").
 * [gate] keeps a load pending (checks the Loading state).
 *
 * Product reads answer from [products] (unknown ids are left out, like the real one):
 * - [getProducts] fails with [productsError] when set; [getProductsCalls] records the ids of each call.
 * - [getProduct] fails with [productError] when set, or [DataError.Unknown] for an unknown id.
 * - [getProductsInCategory] fails with [categoryError] when set.
 *
 * - [getProductsInStore] fails with [storeProductsError] when set.
 * - [getAllProducts] fails with [productsError] when set; [getAllStores] with [storeError].
 *
 * [getStore] answers from [stores], or fails with [storeError] when set.
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

    var productError: DataError? = null
    var categoryError: DataError? = null
    var storeProductsError: DataError? = null

    var stores: List<Store> = emptyList()
    var storeError: DataError? = null

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

    override suspend fun getProduct(id: String): Result<Product, DataError> {
        gate.pass()
        productError?.let { return Result.Error(it) }
        return products.find { it.id == id }?.let { Result.Success(it) } ?: Result.Error(DataError.Unknown)
    }

    override suspend fun getStore(id: String): Result<Store, DataError> {
        storeError?.let { return Result.Error(it) }
        return stores.find { it.id == id }?.let { Result.Success(it) } ?: Result.Error(DataError.Unknown)
    }

    override suspend fun getProductsInCategory(categoryId: String, limit: Int): Result<List<Product>, DataError> {
        categoryError?.let { return Result.Error(it) }
        return Result.Success(products.filter { it.categoryId == categoryId }.take(limit))
    }

    override suspend fun getProductsInStore(storeId: String): Result<List<Product>, DataError> {
        storeProductsError?.let { return Result.Error(it) }
        return Result.Success(products.filter { it.storeId == storeId })
    }

    override suspend fun getAllProducts(): Result<List<Product>, DataError> {
        gate.pass()
        productsError?.let { return Result.Error(it) }
        return Result.Success(products)
    }

    override suspend fun getAllStores(): Result<List<Store>, DataError> {
        storeError?.let { return Result.Error(it) }
        return Result.Success(stores)
    }
}
