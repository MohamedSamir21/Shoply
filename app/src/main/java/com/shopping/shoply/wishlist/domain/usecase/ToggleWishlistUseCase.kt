package com.shopping.shoply.wishlist.domain.usecase

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.wishlist.domain.WishlistRepository
import javax.inject.Inject

/**
 * Flips a product's saved state: removes it when [isSaved] is true, saves it otherwise.
 *
 * The caller passes [isSaved] (what the heart shows now) instead of the use case reading it,
 * so the result always matches what the user tapped.
 *
 * @return [WishlistChange.SAVED] or [WishlistChange.REMOVED], or the repository's error unchanged.
 */
class ToggleWishlistUseCase @Inject constructor(
    private val wishlistRepository: WishlistRepository,
) {
    suspend operator fun invoke(productId: String, isSaved: Boolean): Result<WishlistChange, DataError> {
        val (result, change) =
            if (isSaved) wishlistRepository.remove(productId) to WishlistChange.REMOVED
            else wishlistRepository.add(productId) to WishlistChange.SAVED
        return when (result) {
            is Result.Error -> Result.Error(result.error)
            is Result.Success -> Result.Success(change)
        }
    }
}

/** What a toggle did. The UI uses it to pick the snackbar ("Saved" / "Removed"). */
enum class WishlistChange { SAVED, REMOVED }
