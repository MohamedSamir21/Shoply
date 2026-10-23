package com.shopping.shoply.catalog.domain

import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
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
}
