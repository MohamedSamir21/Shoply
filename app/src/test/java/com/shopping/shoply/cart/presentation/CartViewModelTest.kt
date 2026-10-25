package com.shopping.shoply.cart.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.aCartItem
import com.shopping.shoply.testutil.aStore
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import com.shopping.shoply.testutil.fakes.FakePromoCodeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("CartViewModelTest")
class CartViewModelTest {
    val fakeCartRepository = FakeCartRepository()
    val fakePromoCodeRepository = FakePromoCodeRepository(codes = mapOf("SHOPLY10" to 10))
    val fakeCatalogRepository = FakeCatalogRepository()

    // $20 in store "volt" (fee $1.99): below $35, so delivery is charged.
    val item = aCartItem(id = "c1", storeId = "volt", priceCents = 1000, quantity = 2)

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut(): CartViewModel {
        fakeCatalogRepository.stores = listOf(aStore(id = "volt", deliveryFeeCents = 199))
        return CartViewModel(fakeCartRepository, fakePromoCodeRepository, fakeCatalogRepository)
    }

    @Test
    @DisplayName("state is Loading until the first list, then Empty for an empty cart")
    fun `state is Loading until the first list, then Empty for an empty cart`() = runTest(main.dispatcher) {
        val sut = createSut()

        assertThat(sut.state.value.body).isEqualTo(CartBody.Loading)

        fakeCartRepository.items.value = emptyList()

        assertThat(sut.state.value.body).isEqualTo(CartBody.Empty)
    }

    @Test
    @DisplayName("items show as Content with totals that include the store's fee, and update live")
    fun `items show as Content with totals, and update live`() = runTest(main.dispatcher) {
        fakeCartRepository.items.value = listOf(item)
        val sut = createSut()

        assertThat(sut.state.value.body).isEqualTo(CartBody.Content(listOf(item)))
        assertThat(sut.state.value.totals.totalCents).isEqualTo(2000 + 199)

        fakeCartRepository.items.value = listOf(item.copy(quantity = 4))

        assertThat(sut.state.value.totals.totalCents).isEqualTo(4000)
    }

    @Test
    @DisplayName("Increase and Decrease set the quantity one up or down")
    fun `Increase and Decrease set the quantity one up or down`() = runTest(main.dispatcher) {
        fakeCartRepository.items.value = listOf(item)
        val sut = createSut()

        sut.onIntent(CartIntent.Increase("c1"))
        sut.onIntent(CartIntent.Decrease("c1"))

        assertThat(fakeCartRepository.setQuantityCalls).containsExactly("c1" to 3, "c1" to 1).inOrder()
    }

    @Test
    @DisplayName("Decrease at 1 and Increase at 20 do nothing")
    fun `Decrease at 1 and Increase at 20 do nothing`() = runTest(main.dispatcher) {
        fakeCartRepository.items.value = listOf(
            item.copy(id = "one", quantity = 1),
            item.copy(id = "max", quantity = 20),
        )
        val sut = createSut()

        sut.onIntent(CartIntent.Decrease("one"))
        sut.onIntent(CartIntent.Increase("max"))

        assertThat(fakeCartRepository.setQuantityCalls).isEmpty()
    }

    @Test
    @DisplayName("Remove removes the item and says so; a failure says UPDATE_FAILED")
    fun `Remove removes the item and says so, a failure says UPDATE_FAILED`() = runTest(main.dispatcher) {
        fakeCartRepository.items.value = listOf(item)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(CartIntent.Remove("c1"))
            assertThat(awaitItem()).isEqualTo(CartEvent.ShowMessage(CartMessage.REMOVED))
            assertThat(fakeCartRepository.removeCalls).containsExactly("c1")

            fakeCartRepository.result = Result.Error(DataError.Unknown)
            sut.onIntent(CartIntent.Remove("c1"))
            assertThat(awaitItem()).isEqualTo(CartEvent.ShowMessage(CartMessage.UPDATE_FAILED))
        }
    }

    @Test
    @DisplayName("ApplyPromo trims and uppercases the code, applies it, and clears the field")
    fun `ApplyPromo trims and uppercases the code, applies it, and clears the field`() = runTest(main.dispatcher) {
        fakeCartRepository.items.value = listOf(item)
        val sut = createSut()

        sut.onIntent(CartIntent.PromoInputChanged("  shoply10 "))
        sut.onIntent(CartIntent.ApplyPromo)

        assertThat(fakePromoCodeRepository.lookups).containsExactly("SHOPLY10")
        assertThat(sut.state.value.promo).isEqualTo(AppliedPromo("SHOPLY10", 10))
        assertThat(sut.state.value.promoInput).isEmpty()
        assertThat(sut.state.value.totals.discountCents).isEqualTo(200)
    }

    @Test
    @DisplayName("an unknown code marks the field invalid until the user types again")
    fun `an unknown code marks the field invalid until the user types again`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(CartIntent.PromoInputChanged("NOPE"))
        sut.onIntent(CartIntent.ApplyPromo)

        assertThat(sut.state.value.promoInvalid).isTrue()
        assertThat(sut.state.value.promo).isNull()

        sut.onIntent(CartIntent.PromoInputChanged("NOPE1"))

        assertThat(sut.state.value.promoInvalid).isFalse()
    }

    @Test
    @DisplayName("a failed lookup says PROMO_FAILED and keeps the input")
    fun `a failed lookup says PROMO_FAILED and keeps the input`() = runTest(main.dispatcher) {
        fakePromoCodeRepository.error = DataError.Network
        val sut = createSut()

        sut.events.test {
            sut.onIntent(CartIntent.PromoInputChanged("SHOPLY10"))
            sut.onIntent(CartIntent.ApplyPromo)

            assertThat(awaitItem()).isEqualTo(CartEvent.ShowMessage(CartMessage.PROMO_FAILED))
            assertThat(sut.state.value.promoInput).isEqualTo("SHOPLY10")
            assertThat(sut.state.value.applyingPromo).isFalse()
        }
    }

    @Test
    @DisplayName("Apply is ignored while a code is being checked")
    fun `Apply is ignored while a code is being checked`() = runTest(main.dispatcher) {
        val sut = createSut()
        fakePromoCodeRepository.gate.hold()

        sut.onIntent(CartIntent.PromoInputChanged("SHOPLY10"))
        sut.onIntent(CartIntent.ApplyPromo)
        sut.onIntent(CartIntent.ApplyPromo)

        assertThat(sut.state.value.applyingPromo).isTrue()
        assertThat(fakePromoCodeRepository.lookups).hasSize(1)
        fakePromoCodeRepository.gate.release()
    }

    @Test
    @DisplayName("RemovePromo clears the applied code")
    fun `RemovePromo clears the applied code`() = runTest(main.dispatcher) {
        fakeCartRepository.items.value = listOf(item)
        val sut = createSut()
        sut.onIntent(CartIntent.PromoInputChanged("SHOPLY10"))
        sut.onIntent(CartIntent.ApplyPromo)

        sut.onIntent(CartIntent.RemovePromo)

        assertThat(sut.state.value.promo).isNull()
        assertThat(sut.state.value.totals.discountCents).isEqualTo(0)
    }
}
