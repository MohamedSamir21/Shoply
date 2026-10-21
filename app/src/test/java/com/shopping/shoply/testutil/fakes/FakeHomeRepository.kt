package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.home.domain.model.HomeFeed
import com.shopping.shoply.home.domain.repository.HomeRepository
import com.shopping.shoply.testutil.aHomeFeed

/**
 * [getFeedCalls] counts loads (checks "Retry reloads" and "live updates don't reload").
 * [gate] keeps a load pending (checks the Loading state).
 */
class FakeHomeRepository(
    var result: Result<HomeFeed, DataError> = Result.Success(aHomeFeed()),
) : HomeRepository {
    val gate = Gate()
    var getFeedCalls = 0
        private set

    override suspend fun getFeed(): Result<HomeFeed, DataError> {
        getFeedCalls++
        gate.pass()
        return result
    }
}
