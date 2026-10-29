package com.shopping.shoply.catalog.domain

import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

/** Read access to the shared catalog. */
interface CatalogRepository {

    /**
     * Every category, in display order. One-shot read.
     *
     * @return [DataError.Network] when offline with nothing cached; [DataError.Unknown] for any
     *   other failure (e.g. permission denied).
     */
    suspend fun getCategories(): Result<List<Category>, DataError>

    /**
     * The products with these [ids], in no particular order. Ids with no product are left out.
     * One-shot read.
     *
     * @return the same errors as [getCategories].
     */
    suspend fun getProducts(ids: Set<String>): Result<List<Product>, DataError>

    /**
     * One product. One-shot read.
     *
     * @return the same errors as [getCategories]; a missing product is [DataError.Unknown].
     */
    suspend fun getProduct(id: String): Result<Product, DataError>

    /** One store. Same errors as [getProduct]. */
    suspend fun getStore(id: String): Result<Store, DataError>

    /** Up to [limit] products of [categoryId], in display order. Same errors as [getCategories]. */
    suspend fun getProductsInCategory(categoryId: String, limit: Int): Result<List<Product>, DataError>

    /** Every product of [storeId], in display order. Same errors as [getCategories]. */
    suspend fun getProductsInStore(storeId: String): Result<List<Product>, DataError>
}
