package com.shopping.shoply.checkout.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.cart.presentation.AppliedPromo
import com.shopping.shoply.checkout.domain.model.DeliveryOption
import com.shopping.shoply.checkout.domain.model.PaymentChoice
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.NowProvider
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.aCartItem
import com.shopping.shoply.testutil.aPaymentCard
import com.shopping.shoply.testutil.aStore
import com.shopping.shoply.testutil.anAddress
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import com.shopping.shoply.testutil.fakes.FakeOrderRepository
import com.shopping.shoply.testutil.fakes.FakePaymentRepository
import com.shopping.shoply.testutil.fakes.FakeProfileRepository
import com.shopping.shoply.testutil.fakes.FakePromoCodeRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Calendar

@DisplayName("CheckoutViewModelTest")
class CheckoutViewModelTest {
    val fakeCartRepository = FakeCartRepository()
    val fakePromoCodeRepository = FakePromoCodeRepository(codes = mapOf("SHOPLY10" to 10))
    val fakeCatalogRepository = FakeCatalogRepository()
    val fakeProfileRepository = FakeProfileRepository()
    val fakePaymentRepository = FakePaymentRepository()
    val fakeOrderRepository = FakeOrderRepository()

    // $20 in store "volt" (fee $1.99): below $35, so delivery is charged.
    val item = aCartItem(id = "c1", storeId = "volt", priceCents = 1000, quantity = 2)
    val home = anAddress(id = "home", primary = true)
    val work = anAddress(id = "work", label = "Work", primary = false)
    val visa = aPaymentCard(id = "visa", primary = true)
    val mastercard = aPaymentCard(id = "mc", primary = false)

    // 11:30 on Oct 27, 2026: the 10 AM and 12 PM slots are full, so 2 PM is the first free one.
    val now: Long = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, 27, 11, 30)
    }.timeInMillis

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut(promo: String? = null): CheckoutViewModel {
        fakeCatalogRepository.stores = listOf(aStore(id = "volt", deliveryFeeCents = 199))
        fakeCartRepository.items.value = listOf(item)
        fakeProfileRepository.addresses.value = listOf(home, work)
        fakePaymentRepository.cards.value = listOf(visa, mastercard)
        return CheckoutViewModel(
            savedStateHandle = SavedStateHandle(listOfNotNull(promo?.let { PROMO_CODE_ARG to it }).toMap()),
            cartRepository = fakeCartRepository,
            promoCodeRepository = fakePromoCodeRepository,
            catalogRepository = fakeCatalogRepository,
            profileRepository = fakeProfileRepository,
            paymentRepository = fakePaymentRepository,
            orderRepository = fakeOrderRepository,
            nowProvider = NowProvider { now },
        )
    }

    private fun CheckoutViewModel.continueTo(step: Int) = repeat(step) { onIntent(CheckoutIntent.Continue) }

    @Test
    @DisplayName("picks the primary address, Standard on the first free slot, and the primary card")
    fun `picks the defaults`() = runTest(main.dispatcher) {
        val state = createSut().state.value

        assertThat(state.selectedAddressId).isEqualTo("home")
        assertThat(state.speed).isEqualTo(DeliverySpeed.STANDARD)
        assertThat(state.dayIndex).isEqualTo(0)
        assertThat(state.slotHour).isEqualTo(14)
        assertThat(state.payment).isEqualTo(PaymentChoice.Card(visa))
        assertThat(state.totals.totalCents).isEqualTo(2000 + 199)
    }

    @Test
    @DisplayName("Express adds $4.99 to the delivery")
    fun `Express adds 4,99 to the delivery`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(CheckoutIntent.SelectSpeed(DeliverySpeed.EXPRESS))

        assertThat(sut.state.value.totals.deliveryCents).isEqualTo(199 + 499)
    }

    @Test
    @DisplayName("SelectDay picks that day's first slot; a full slot can't be picked")
    fun `SelectDay picks that day's first slot, a full slot can't be picked`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(CheckoutIntent.SelectSlot(12))
        assertThat(sut.state.value.slotHour).isEqualTo(14)

        sut.onIntent(CheckoutIntent.SelectDay(1))
        assertThat(sut.state.value.slotHour).isEqualTo(10)
    }

    @Test
    @DisplayName("Continue is blocked with no address, and with no payment")
    fun `Continue is blocked with no address, and with no payment`() = runTest(main.dispatcher) {
        val sut = createSut()
        fakeProfileRepository.addresses.value = emptyList()
        fakePaymentRepository.cards.value = emptyList()

        sut.onIntent(CheckoutIntent.Continue)
        assertThat(sut.state.value.step).isEqualTo(STEP_ADDRESS)

        fakeProfileRepository.addresses.value = listOf(home)
        sut.continueTo(3)
        assertThat(sut.state.value.step).isEqualTo(STEP_PAYMENT)

        sut.onIntent(CheckoutIntent.SelectPayment(PaymentChoice.CashOnDelivery))
        sut.onIntent(CheckoutIntent.Continue)
        assertThat(sut.state.value.step).isEqualTo(STEP_REVIEW)
    }

    @Test
    @DisplayName("Back steps back, and leaves from the first step")
    fun `Back steps back, and leaves from the first step`() = runTest(main.dispatcher) {
        val sut = createSut()
        sut.continueTo(1)

        sut.events.test {
            sut.onIntent(CheckoutIntent.Back)
            assertThat(sut.state.value.step).isEqualTo(STEP_ADDRESS)

            sut.onIntent(CheckoutIntent.Back)
            assertThat(awaitItem()).isEqualTo(CheckoutEvent.Back)
        }
    }

    @Test
    @DisplayName("GoToStep only goes back")
    fun `GoToStep only goes back`() = runTest(main.dispatcher) {
        val sut = createSut()
        sut.continueTo(2)

        sut.onIntent(CheckoutIntent.GoToStep(STEP_REVIEW))
        assertThat(sut.state.value.step).isEqualTo(STEP_PAYMENT)

        sut.onIntent(CheckoutIntent.GoToStep(STEP_ADDRESS))
        assertThat(sut.state.value.step).isEqualTo(STEP_ADDRESS)
    }

    @Test
    @DisplayName("PlaceOrder sends what was picked and reports the new order id")
    fun `PlaceOrder sends what was picked and reports the new order id`() = runTest(main.dispatcher) {
        val sut = createSut(promo = "SHOPLY10")
        sut.onIntent(CheckoutIntent.SelectAddress("work"))
        sut.onIntent(CheckoutIntent.SelectPayment(PaymentChoice.Installments(mastercard, months = 3)))
        sut.continueTo(3)

        sut.events.test {
            sut.onIntent(CheckoutIntent.PlaceOrder)

            assertThat(awaitItem()).isEqualTo(CheckoutEvent.OrderPlaced("order1"))
            val request = fakeOrderRepository.requests.single()
            assertThat(request.items).containsExactly(item)
            assertThat(request.address).isEqualTo(work)
            assertThat(request.delivery).isEqualTo(DeliveryOption.Standard(sut.state.value.days[0].startMillis, 14))
            assertThat(request.payment).isEqualTo(PaymentChoice.Installments(mastercard, months = 3))
            assertThat(request.promoCode).isEqualTo("SHOPLY10")
            assertThat(request.totals.totalCents).isEqualTo(2000 + 199 - 200)
        }
    }

    @Test
    @DisplayName("a failed order says ORDER_FAILED and lets the user try again")
    fun `a failed order says ORDER_FAILED and lets the user try again`() = runTest(main.dispatcher) {
        fakeOrderRepository.result = Result.Error(DataError.Unknown)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(CheckoutIntent.PlaceOrder)

            assertThat(awaitItem()).isEqualTo(CheckoutEvent.ShowMessage(CheckoutMessage.ORDER_FAILED))
            assertThat(sut.state.value.placing).isFalse()
        }
    }

    @Test
    @DisplayName("PlaceOrder is ignored while an order is being placed")
    fun `PlaceOrder is ignored while an order is being placed`() = runTest(main.dispatcher) {
        val sut = createSut()
        fakeOrderRepository.gate.hold()

        sut.onIntent(CheckoutIntent.PlaceOrder)
        sut.onIntent(CheckoutIntent.PlaceOrder)

        assertThat(sut.state.value.placing).isTrue()
        assertThat(fakeOrderRepository.requests).hasSize(1)
        fakeOrderRepository.gate.release()
    }

    @Test
    @DisplayName("a valid promo from the Cart is applied; an unknown or failed one gives no discount")
    fun `the promo from the Cart is checked again`() = runTest(main.dispatcher) {
        assertThat(createSut(promo = "SHOPLY10").state.value.promo).isEqualTo(AppliedPromo("SHOPLY10", 10))
        assertThat(createSut(promo = "NOPE").state.value.totals.discountCents).isEqualTo(0)

        fakePromoCodeRepository.error = DataError.Network
        assertThat(createSut(promo = "SHOPLY10").state.value.promo).isNull()
    }
}
