package com.shopping.shoply.categories.presentation

/**
 * User actions that need logic. Navigation taps (search, a category, change address) don't go
 * through the ViewModel: the screen calls its navigation callback directly.
 */
sealed interface CategoriesIntent {
    /** Reload from the Offline state. */
    data object Retry : CategoriesIntent
}
