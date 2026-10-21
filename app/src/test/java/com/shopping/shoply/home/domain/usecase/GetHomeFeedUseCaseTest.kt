package com.shopping.shoply.home.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.home.domain.model.HomeFeed
import com.shopping.shoply.home.domain.repository.HomeRepository
import com.shopping.shoply.testutil.aHomeFeed
import com.shopping.shoply.testutil.products
import com.shopping.shoply.testutil.promos
import com.shopping.shoply.testutil.stores
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GetHomeFeedUseCaseTest")
class GetHomeFeedUseCaseTest {


    @Test
    @DisplayName("loads small home feed without trimming when they're under maxes")
    fun `loads home feed without trimming when they're under maxes`() = runTest {
        val repository = HomeRepository {
            Result.Success(
                aHomeFeed()
            )
        }
        val getHomeFeedUseCase = GetHomeFeedUseCase(repository)

        assertThat(
            getHomeFeedUseCase()
        ).isEqualTo(
            Result.Success<HomeFeed, DataError>(aHomeFeed())
        )
    }

    @Test
    @DisplayName("trims home feed for a specific section when it exceeds its max")
    fun `trims home feed for a specific section when it exceeds its max`() = runTest {
        val repository = HomeRepository {
            Result.Success(
                aHomeFeed(
                    promos = promos(4)
                )
            )
        }
        val getHomeFeedUseCase = GetHomeFeedUseCase(repository)

        assertThat(
            getHomeFeedUseCase()
        ).isEqualTo(
            Result.Success<HomeFeed, DataError>(
                aHomeFeed(
                    promos = promos(3)
                )
            )
        )
    }

    @Test
    @DisplayName("trims home feed for all sections when they all exceed their maxes")
    fun `trims home feed for all sections when they all exceed their maxes`() = runTest {
        val repository = HomeRepository {
            Result.Success(
                aHomeFeed(
                    promos = promos(4),
                    featuredStores = stores(5),
                    popular = products(11),
                    recommended = products(21)
                )
            )
        }
        val getHomeFeedUseCase = GetHomeFeedUseCase(repository)

        assertThat(
            getHomeFeedUseCase()
        ).isEqualTo(
            Result.Success<HomeFeed, DataError>(
                aHomeFeed(
                    promos = promos(GetHomeFeedUseCase.MAX_PROMOS),
                    featuredStores = stores(GetHomeFeedUseCase.MAX_FEATURED_STORES),
                    popular = products(GetHomeFeedUseCase.MAX_POPULAR),
                    recommended = products(GetHomeFeedUseCase.MAX_RECOMMENDED)
                )
            )
        )
    }

    @Test
    @DisplayName("reports error when there's issue")
    fun `reports error when there's issue`() = runTest {
        val repository = HomeRepository {
            Result.Error(DataError.Unknown)
        }
        val getHomeFeedUseCase = GetHomeFeedUseCase(repository)

        assertThat(
            getHomeFeedUseCase()
        ).isEqualTo(
            Result.Error<HomeFeed, DataError>(
                DataError.Unknown
            )
        )
    }
}