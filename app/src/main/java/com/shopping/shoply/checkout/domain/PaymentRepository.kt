package com.shopping.shoply.checkout.domain

import com.shopping.shoply.checkout.domain.model.PaymentCard
import kotlinx.coroutines.flow.Flow

/** The signed-in user's saved cards. */
interface PaymentRepository {

    /** Live list, primary first. Never completes or throws; keeps the last value when offline. */
    fun observeCards(): Flow<List<PaymentCard>>
}
