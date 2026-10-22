package com.shopping.shoply.home.presentation

import app.cash.turbine.test
import app.cash.turbine.turbineScope
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.CurrentHourProvider
import com.shopping.shoply.home.domain.rules.Greeting
import com.shopping.shoply.home.domain.usecase.GetHomeFeedUseCase
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.aHomeFeed
import com.shopping.shoply.testutil.aProduct
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeHomeRepository
import com.shopping.shoply.testutil.fakes.FakeNotificationsRepository
import com.shopping.shoply.testutil.fakes.FakeProfileRepository
import com.shopping.shoply.testutil.fakes.FakeWishlistRepository
import com.shopping.shoply.wishlist.domain.usecase.ToggleWishlistUseCase
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("HomeViewModelTest")
class HomeViewModelTest {
    val fakeHomeRepository = FakeHomeRepository()
    val fakeWishlistRepository = FakeWishlistRepository()
    val fakeCartRepository = FakeCartRepository()
    val fakeProfileRepository = FakeProfileRepository()
    val fakeNotificationsRepository = FakeNotificationsRepository()
    var currentHourProvider = CurrentHourProvider { 11 }

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut(): HomeViewModel {
        return HomeViewModel(
            getHomeFeed = GetHomeFeedUseCase(fakeHomeRepository),
            toggleWishlist = ToggleWishlistUseCase(fakeWishlistRepository),
            quickAddToCart = QuickAddToCartUseCase(fakeCartRepository),
            wishlistRepository = fakeWishlistRepository,
            profileRepository = fakeProfileRepository,
            notificationsRepository = fakeNotificationsRepository,
            currentHourProvider = currentHourProvider
        )
    }

    @Test
    @DisplayName("the viewmodel state is set to Loading correctly")
    fun `the viewmodel state is set to Loading correctly`() = runTest(main.dispatcher) {
        fakeHomeRepository.gate.hold()
        val sut = createSut()

        sut.state.test {
            assertThat(awaitItem().body).isEqualTo(HomeBody.Loading)
        }
        fakeHomeRepository.gate.release()
    }

    @Test
    @DisplayName("the viewmodel is populating its state with Content")
    fun `the viewmodel is populating its state with Content`() =
        runTest(main.dispatcher) {
            val sut = createSut()

            sut.state.test {
                assertThat(awaitItem().body).isEqualTo(HomeBody.Content(aHomeFeed()))
            }
        }

    @Test
    @DisplayName("the viewmodel loaded with no stores near")
    fun `the viewmodel loaded with no stores near`() = runTest(main.dispatcher) {
        fakeHomeRepository.result = Result.Success(aHomeFeed(featuredStores = emptyList()))
        val sut = createSut()

        sut.state.test {
            assertThat(awaitItem().body).isEqualTo(HomeBody.Empty)
        }
    }

    @Test
    @DisplayName("the viewmodel state is set to Offline when there is any error")
    fun `the viewmodel state is set to Offline when there is any error`() =
        runTest(main.dispatcher) {
            fakeHomeRepository.result = Result.Error(DataError.Unknown)
            val sut = createSut()

            sut.state.test {
                assertThat(awaitItem().body).isEqualTo(HomeBody.Offline)
            }
        }

    @Test
    @DisplayName("builds the header from live data")
    fun `builds the header from live data`() = runTest(main.dispatcher) {
        val sut = createSut()
        fakeProfileRepository.displayName.update { "Mohamed Samir" }
        fakeProfileRepository.primaryAddress.update {
            DeliveryAddress(
                "Home",
                "24 Elm Street, Cairo"
            )
        }
        fakeWishlistRepository.ids.update { setOf("id1", "id2") }
        fakeNotificationsRepository.unreadCount.update { 1 }

        sut.state.test {
            val state = awaitItem()
            assertThat(state.greeting).isEqualTo(Greeting.MORNING)
            assertThat(state.firstName).isEqualTo("Mohamed")
            assertThat(state.initials).isEqualTo("MS")
            assertThat(state.addressLine).isEqualTo("Home · 24 Elm Street")
            assertThat(state.hasUnreadNotifications).isTrue()
            assertThat(state.wishlist).isEqualTo(setOf("id1", "id2"))
        }
    }

    @Test
    @DisplayName("sets the viewmodel state to have a null address when there's no delivery address")
    fun `sets the viewmodel state to have a null address when there's no delivery address`() =
        runTest(main.dispatcher) {
            fakeProfileRepository.primaryAddress.update { null }
            val sut = createSut()
            sut.state.test {
                assertThat(awaitItem().addressLine).isNull()
            }
        }

    @Test
    @DisplayName("live updates doesn't reload the feed")
    fun `live updates doesn't reload the feed`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.state.test {
            assertThat(awaitItem().hasUnreadNotifications).isFalse()
            fakeNotificationsRepository.unreadCount.update { 2 }
            assertThat(awaitItem().hasUnreadNotifications).isTrue()
        }
        assertThat(fakeHomeRepository.getFeedCalls).isEqualTo(1)
    }

    @Test
    @DisplayName("retries loading content when there is an error")
    fun `retries loading content when there is an error`() = runTest(main.dispatcher) {
        fakeHomeRepository.result = Result.Error(DataError.Unknown)
        val sut = createSut()

        sut.state.test {
            assertThat(awaitItem().body).isEqualTo(HomeBody.Offline)
            fakeHomeRepository.result = Result.Success(aHomeFeed())
            fakeHomeRepository.gate.hold()
            sut.onIntent(HomeIntent.Retry)
            assertThat(awaitItem().body).isEqualTo(HomeBody.Loading)
            fakeHomeRepository.gate.release()
            assertThat(awaitItem().body).isEqualTo(HomeBody.Content(aHomeFeed()))
        }

        assertThat(fakeHomeRepository.getFeedCalls).isEqualTo(2)
    }

    @Test
    @DisplayName("heart is filled while the product is saving in a wishlist")
    fun `heart is filled while the product is saving in a wishlist`() = runTest(main.dispatcher) {
        val productId = "product1"
        val sut = createSut()

        turbineScope {
            val states = sut.state.testIn(backgroundScope)
            val events = sut.events.testIn(backgroundScope)
            assertThat(states.awaitItem().wishlist).isEmpty()

            fakeWishlistRepository.gate.hold()
            sut.onIntent(HomeIntent.ToggleWishlist(productId))
            assertThat(states.awaitItem().wishlist).containsExactly(productId)

            fakeWishlistRepository.gate.release()
            assertThat(events.awaitItem()).isEqualTo(HomeEvent.ShowMessage(HomeMessage.SAVED_TO_WISHLIST))
            states.cancelAndIgnoreRemainingEvents()
        }
        assertThat(fakeWishlistRepository.addCalls).containsExactly(productId)
    }

    @Test
    @DisplayName("heart is unfilled while the product is being removed from a wishlist")
    fun `heart is unfilled while the product is being removed from a wishlist`() =
        runTest(main.dispatcher) {
            val productId = "product1"
            fakeWishlistRepository.ids.update { setOf(productId) }
            val sut = createSut()

            turbineScope {
                val states = sut.state.testIn(backgroundScope)
                val events = sut.events.testIn(backgroundScope)
                assertThat(states.awaitItem().wishlist).containsExactly(productId)

                fakeWishlistRepository.gate.hold()
                sut.onIntent(HomeIntent.ToggleWishlist(productId))
                assertThat(states.awaitItem().wishlist).isEmpty()

                fakeWishlistRepository.gate.release()
                assertThat(events.awaitItem()).isEqualTo(HomeEvent.ShowMessage(HomeMessage.REMOVED_FROM_WISHLIST))
                states.cancelAndIgnoreRemainingEvents()
            }
            assertThat(fakeWishlistRepository.removeCalls).containsExactly(productId)
        }

    @Test
    @DisplayName("flips heart back to unfilled after failure occurred while saving")
    fun `flips heart back to unfilled after failure occurred while saving`() =
        runTest(main.dispatcher) {
            val productId = "product1"
            fakeWishlistRepository.result = Result.Error(DataError.Unknown)
            val sut = createSut()

            turbineScope {
                val states = sut.state.testIn(backgroundScope)
                val events = sut.events.testIn(backgroundScope)
                assertThat(states.awaitItem().wishlist).isEmpty()

                fakeWishlistRepository.gate.hold()
                sut.onIntent(HomeIntent.ToggleWishlist(productId))
                assertThat(states.awaitItem().wishlist).containsExactly(productId)

                fakeWishlistRepository.gate.release()
                assertThat(states.awaitItem().wishlist).isEmpty()
                assertThat(events.awaitItem()).isEqualTo(HomeEvent.ShowMessage(HomeMessage.WISHLIST_FAILED))
            }
        }

    @Test
    @DisplayName("ignores any attempt to flip heart while current product saving is in progress")
    fun `ignores any attempt to flip heart while current product saving is in progress`() =
        runTest(main.dispatcher) {
            val productId = "product1"
            val sut = createSut()

            sut.state.test {
                assertThat(awaitItem().wishlist).isEmpty()

                fakeWishlistRepository.gate.hold()
                sut.onIntent(HomeIntent.ToggleWishlist(productId))
                sut.onIntent(HomeIntent.ToggleWishlist(productId))
                assertThat(awaitItem().wishlist).containsExactly(productId)

                fakeWishlistRepository.gate.release()
                cancelAndIgnoreRemainingEvents()
            }
            assertThat(fakeWishlistRepository.addCalls).containsExactly(productId)
            assertThat(fakeWishlistRepository.removeCalls).isEmpty()
        }

    @Test
    @DisplayName("quick add sends ADDED_TO_CART and adds the product")
    fun `quick add sends ADDED_TO_CART and adds the product`() = runTest(main.dispatcher) {
        val product = aProduct()
        val sut = createSut()

        sut.events.test {
            sut.onIntent(HomeIntent.QuickAdd(product))
            assertThat(awaitItem()).isEqualTo(HomeEvent.ShowMessage(HomeMessage.ADDED_TO_CART))
        }
        assertThat(fakeCartRepository.addCalls.single().first).isEqualTo(product)
    }

    @Test
    @DisplayName("quick add sends CART_FAILED when adding fails")
    fun `quick add sends CART_FAILED when adding fails`() = runTest(main.dispatcher) {
        fakeCartRepository.result = Result.Error(DataError.Unknown)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(HomeIntent.QuickAdd(aProduct()))
            assertThat(awaitItem()).isEqualTo(HomeEvent.ShowMessage(HomeMessage.CART_FAILED))
        }
    }
}