package com.shopping.shoply.home.domain.repository

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.home.domain.model.HomeFeed

/** Source of the Home catalog content. The catalog rarely changes, so it's loaded once, not observed. */
fun interface HomeRepository {

    /**
     * Loads the whole feed in one call.
     *
     * @return the feed, or [DataError.Network] when offline or timed out, [DataError.Unknown] otherwise.
     */
    suspend fun getFeed(): Result<HomeFeed, DataError>
}
