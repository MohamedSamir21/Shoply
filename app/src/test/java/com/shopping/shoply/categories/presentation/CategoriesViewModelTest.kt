package com.shopping.shoply.categories.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.categories
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("CategoriesViewModelTest")
class CategoriesViewModelTest {
    val fakeCatalogRepository = FakeCatalogRepository()

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut() = CategoriesViewModel(catalogRepository = fakeCatalogRepository)

    @Test
    @DisplayName("is Loading while the categories load")
    fun `is Loading while the categories load`() = runTest(main.dispatcher) {
        fakeCatalogRepository.gate.hold()
        val sut = createSut()

        sut.state.test {
            assertThat(awaitItem()).isEqualTo(CategoriesState.Loading)
        }
        fakeCatalogRepository.gate.release()
    }

    @Test
    @DisplayName("shows the loaded categories as Content")
    fun `shows the loaded categories as Content`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.state.test {
            assertThat(awaitItem()).isEqualTo(CategoriesState.Content(categories(3)))
        }
    }

    @Test
    @DisplayName("is Empty when there are no categories")
    fun `is Empty when there are no categories`() = runTest(main.dispatcher) {
        fakeCatalogRepository.result = Result.Success(emptyList())
        val sut = createSut()

        sut.state.test {
            assertThat(awaitItem()).isEqualTo(CategoriesState.Empty)
        }
    }

    @Test
    @DisplayName("is Offline when the load fails")
    fun `is Offline when the load fails`() = runTest(main.dispatcher) {
        fakeCatalogRepository.result = Result.Error(DataError.Network)
        val sut = createSut()

        sut.state.test {
            assertThat(awaitItem()).isEqualTo(CategoriesState.Offline)
        }
    }

    @Test
    @DisplayName("Retry goes Offline -> Loading -> Content and loads again")
    fun `Retry goes Offline - Loading - Content and loads again`() = runTest(main.dispatcher) {
        fakeCatalogRepository.result = Result.Error(DataError.Network)
        val sut = createSut()

        sut.state.test {
            assertThat(awaitItem()).isEqualTo(CategoriesState.Offline)

            fakeCatalogRepository.result = Result.Success(categories(2))
            fakeCatalogRepository.gate.hold()
            sut.onIntent(CategoriesIntent.Retry)
            assertThat(awaitItem()).isEqualTo(CategoriesState.Loading)

            fakeCatalogRepository.gate.release()
            assertThat(awaitItem()).isEqualTo(CategoriesState.Content(categories(2)))
        }
        assertThat(fakeCatalogRepository.getCategoriesCalls).isEqualTo(2)
    }
}
