package com.shopping.shoply.payments

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.NowProvider
import com.shopping.shoply.payments.domain.CardWalletRepository
import com.shopping.shoply.payments.domain.rules.CardField
import com.shopping.shoply.payments.domain.rules.CardForm
import com.shopping.shoply.payments.domain.rules.cardBrandOf
import com.shopping.shoply.payments.domain.rules.cardFrom
import com.shopping.shoply.payments.domain.rules.formatCardNumberInput
import com.shopping.shoply.payments.domain.rules.formatExpiryInput
import com.shopping.shoply.payments.domain.rules.invalidCardFields
import com.shopping.shoply.payments.domain.rules.isValidCardNumber
import com.shopping.shoply.payments.domain.rules.isValidExpiry
import com.shopping.shoply.payments.presentation.PaymentMessage
import com.shopping.shoply.payments.presentation.PaymentMethodsEvent
import com.shopping.shoply.payments.presentation.PaymentMethodsIntent
import com.shopping.shoply.payments.presentation.PaymentMethodsViewModel
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.fakes.FakePaymentRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Calendar

@DisplayName("PaymentMethodsTest")
class PaymentMethodsTest {
    val visa = PaymentCard("c1", CardBrand.VISA, "4821", "08/28", primary = true)
    val master = PaymentCard("c2", CardBrand.MASTERCARD, "0937", "11/27", primary = false)
    val validForm = CardForm(name = "Ahmed", number = "4242 4242 4242 4242", expiry = "12/30", cvv = "123")

    val fakePaymentRepository = FakePaymentRepository().apply { cards.value = listOf(visa, master) }
    val fakeWallet = FakeWallet()

    // Oct 2026.
    val now = NowProvider { Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 15) }.timeInMillis }

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut() = PaymentMethodsViewModel(fakePaymentRepository, fakeWallet, now)

    @Test
    @DisplayName("card rules: brand, Luhn, expiry (valid through its month), typing helpers")
    fun `card rules`() {
        assertThat(cardBrandOf("4242")).isEqualTo(CardBrand.VISA)
        assertThat(cardBrandOf("5500 0000")).isEqualTo(CardBrand.MASTERCARD)
        assertThat(cardBrandOf("2221 0000")).isEqualTo(CardBrand.MASTERCARD)
        assertThat(cardBrandOf("3782")).isEqualTo(CardBrand.OTHER)
        assertThat(isValidCardNumber("4242 4242 4242 4242")).isTrue()
        assertThat(isValidCardNumber("4242 4242 4242 4241")).isFalse()
        assertThat(isValidExpiry("10/26", 2026, 10)).isTrue()
        assertThat(isValidExpiry("09/26", 2026, 10)).isFalse()
        assertThat(isValidExpiry("13/30", 2026, 10)).isFalse()
        assertThat(formatExpiryInput("0828")).isEqualTo("08/28")
        assertThat(formatCardNumberInput("42424242")).isEqualTo("4242 4242")
    }

    @Test
    @DisplayName("invalidCardFields and cardFrom: only brand, last 4 and expiry are kept")
    fun `form validation and mapping`() {
        assertThat(invalidCardFields(validForm, 2026, 10)).isEmpty()
        assertThat(invalidCardFields(CardForm(cvv = "12"), 2026, 10)).containsExactly(CardField.NAME, CardField.NUMBER, CardField.EXPIRY, CardField.CVV)
        assertThat(cardFrom(validForm, isFirst = true)).isEqualTo(PaymentCard("", CardBrand.VISA, "4242", "12/30", primary = true))
    }

    @Test
    @DisplayName("SaveCard: errors first, then a valid card is added and the sheet closes")
    fun `save card`() = runTest(main.dispatcher) {
        val sut = createSut()
        sut.onIntent(PaymentMethodsIntent.OpenAddCard)
        sut.onIntent(PaymentMethodsIntent.SaveCard)
        assertThat(sut.state.value.errors).isNotEmpty()
        assertThat(fakeWallet.added).isEmpty()

        sut.events.test {
            sut.onIntent(PaymentMethodsIntent.Change(validForm.copy(makeDefault = true)))
            sut.onIntent(PaymentMethodsIntent.SaveCard)
            assertThat(awaitItem()).isEqualTo(PaymentMethodsEvent.ShowMessage(PaymentMessage.CARD_SAVED))
        }
        assertThat(fakeWallet.added.single().second).containsExactly("c1")
        assertThat(sut.state.value.sheetOpen).isFalse()
    }

    @Test
    @DisplayName("SetDefault demotes the old default; removing the default promotes the next card")
    fun `default and remove`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(PaymentMethodsIntent.SetDefault("c2"))
        assertThat(fakeWallet.defaults.single()).isEqualTo("c2" to listOf("c1"))

        sut.onIntent(PaymentMethodsIntent.Remove("c1"))
        assertThat(fakeWallet.removed.single()).isEqualTo("c1" to "c2")
    }

    @Test
    @DisplayName("a failed write says FAILED and keeps the sheet open")
    fun `failure keeps the sheet`() = runTest(main.dispatcher) {
        fakeWallet.error = DataError.Unknown
        val sut = createSut()
        sut.onIntent(PaymentMethodsIntent.OpenAddCard)
        sut.onIntent(PaymentMethodsIntent.Change(validForm))

        sut.events.test {
            sut.onIntent(PaymentMethodsIntent.SaveCard)
            assertThat(awaitItem()).isEqualTo(PaymentMethodsEvent.ShowMessage(PaymentMessage.FAILED))
        }
        assertThat(sut.state.value.sheetOpen).isTrue()
    }

    class FakeWallet : CardWalletRepository {
        var error: DataError? = null
        val added = mutableListOf<Pair<PaymentCard, List<String>>>()
        val defaults = mutableListOf<Pair<String, List<String>>>()
        val removed = mutableListOf<Pair<String, String?>>()

        override suspend fun add(card: PaymentCard, demoteIds: List<String>): Result<String, DataError> {
            added += card to demoteIds
            return error?.let { Result.Error(it) } ?: Result.Success("new")
        }

        override suspend fun setDefault(id: String, demoteIds: List<String>): Result<Unit, DataError> {
            defaults += id to demoteIds
            return error?.let { Result.Error(it) } ?: Result.Success(Unit)
        }

        override suspend fun remove(id: String, promoteId: String?): Result<Unit, DataError> {
            removed += id to promoteId
            return error?.let { Result.Error(it) } ?: Result.Success(Unit)
        }
    }
}
