package com.shopping.shoply.categories.presentation

import com.shopping.shoply.catalog.domain.model.Category

/** What the Categories screen shows below its top bar. */
sealed interface CategoriesState {
    data object Loading : CategoriesState

    /** Loaded, but there are no categories. */
    data object Empty : CategoriesState

    /** Any load error (network or other). */
    data object Offline : CategoriesState

    data class Content(val categories: List<Category>) : CategoriesState
}
