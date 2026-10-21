package com.shopping.shoply.home.domain.usecase

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.home.domain.model.HomeFeed
import com.shopping.shoply.home.domain.repository.HomeRepository
import javax.inject.Inject

/**
 * Loads the Home feed and trims each section to what the screen shows:
 * at most [MAX_PROMOS] promos, [MAX_FEATURED_STORES] featured stores, [MAX_POPULAR] popular and
 * [MAX_RECOMMENDED] recommended products. Categories are not trimmed. Order is kept.
 *
 * Errors from the repository pass through unchanged.
 */
class GetHomeFeedUseCase @Inject constructor(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(): Result<HomeFeed, DataError> =
        when (val result = homeRepository.getFeed()) {
            is Result.Error -> result
            is Result.Success -> Result.Success(
                result.data.run {
                    copy(
                        promos = promos.take(MAX_PROMOS),
                        featuredStores = featuredStores.take(MAX_FEATURED_STORES),
                        popular = popular.take(MAX_POPULAR),
                        recommended = recommended.take(MAX_RECOMMENDED),
                    )
                }
            )
        }

    companion object {
        const val MAX_PROMOS = 3
        const val MAX_FEATURED_STORES = 4
        const val MAX_POPULAR = 10
        const val MAX_RECOMMENDED = 20
    }
}
