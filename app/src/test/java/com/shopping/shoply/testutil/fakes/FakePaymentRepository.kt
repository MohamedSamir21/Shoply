package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.checkout.domain.PaymentRepository
import com.shopping.shoply.checkout.domain.model.PaymentCard
import kotlinx.coroutines.flow.MutableStateFlow

/** Set [cards] directly to simulate live changes. */
class FakePaymentRepository : PaymentRepository {
    val cards = MutableStateFlow<List<PaymentCard>>(emptyList())

    override fun observeCards() = cards
}
