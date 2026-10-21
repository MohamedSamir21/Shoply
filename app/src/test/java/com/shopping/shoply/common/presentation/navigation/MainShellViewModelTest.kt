package com.shopping.shoply.common.presentation.navigation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("MainShellViewModelTest")
class MainShellViewModelTest {
    val fakeCartRepository = FakeCartRepository()

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    @Test
    @DisplayName("badge is hidden for an empty cart, then follows the cart live")
    fun `badge is hidden for an empty cart, then follows the cart live`() = runTest(main.dispatcher) {
        val sut = MainShellViewModel(fakeCartRepository)

        sut.cartBadge.test {
            assertThat(awaitItem()).isNull()
            fakeCartRepository.totalQuantity.value = 3
            assertThat(awaitItem()).isEqualTo("3")
        }
    }
}
