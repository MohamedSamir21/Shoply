package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.search.domain.RecentSearchesRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** [queries] is what [observe] emits; [save] replaces it. */
class FakeRecentSearchesRepository : RecentSearchesRepository {
    val queries = MutableStateFlow<List<String>>(emptyList())

    override fun observe() = queries

    override suspend fun save(queries: List<String>) {
        this.queries.value = queries
    }
}
