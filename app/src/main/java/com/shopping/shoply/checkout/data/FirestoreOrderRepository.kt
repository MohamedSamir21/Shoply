package com.shopping.shoply.checkout.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.checkout.domain.OrderRepository
import com.shopping.shoply.checkout.domain.model.DeliveryOption
import com.shopping.shoply.checkout.domain.model.OrderRequest
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.checkout.domain.model.PaymentChoice
import com.shopping.shoply.common.data.firestore.writeForCurrentUser
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject
import kotlin.random.Random

/**
 * Orders in `users/{uid}/orders/{SH-12345}`; the id doubles as the order number shown to the user.
 * One batch writes the order and deletes its cart items, so the cart never empties without the
 * order being saved (or the other way round).
 *
 * Schema (shared with the seeded test orders; the Orders screen reads it):
 * - items: [{productId, name, icon, storeId, priceCents, color?, size?, quantity}]
 * - address: {id, label, recipientName, line1, line2, phone}, a copy, so later edits don't change it
 * - delivery: {type: "standard", dayStartMillis, slotStartHour} | {type: "express"}
 * - payment: {type: "card" | "installments" | "cod", cardId?, brand?, last4?, months?}
 * - promoCode?, subtotalCents, deliveryCents, discountCents, totalCents
 * - status: placed | confirmed | packed | on_the_way | delivered | cancelled
 * - timeline: [{status, at, note?}], one entry per status reached
 * - etaAt? (set once a courier has it), createdAt
 */
class FirestoreOrderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : OrderRepository {

    override suspend fun place(request: OrderRequest): Result<String, DataError> {
        val orderId = "SH-${Random.nextInt(10_000, 100_000)}"
        val result = auth.writeForCurrentUser { uid ->
            val user = firestore.collection("users").document(uid)
            val order = user.collection("orders").document(orderId)
            firestore.batch().apply {
                set(order, request.toFirestore())
                request.items.forEach { delete(user.collection("cart").document(it.id)) }
            }.commit()
        }
        return when (result) {
            is Result.Success -> Result.Success(orderId)
            is Result.Error -> Result.Error(result.error)
        }
    }

    private fun OrderRequest.toFirestore() = mapOf(
        "items" to items.map { cartItem ->
            mapOf(
                "productId" to cartItem.productId,
                "name" to cartItem.name,
                "icon" to cartItem.icon.name.lowercase(),
                "storeId" to cartItem.storeId,
                "priceCents" to cartItem.priceCents,
                "color" to cartItem.color,
                "size" to cartItem.size,
                "quantity" to cartItem.quantity,
            )
        },
        "address" to mapOf(
            "id" to address.id,
            "label" to address.label,
            "recipientName" to address.recipientName,
            "line1" to address.line1,
            "line2" to address.line2,
            "phone" to address.phone,
        ),
        "delivery" to when (delivery) {
            is DeliveryOption.Standard -> mapOf(
                "type" to "standard",
                "dayStartMillis" to delivery.dayStartMillis,
                "slotStartHour" to delivery.slotStartHour,
            )
            DeliveryOption.Express -> mapOf("type" to "express")
        },
        "payment" to when (payment) {
            is PaymentChoice.Card -> mapOf("type" to "card") + payment.card.toFirestore()
            is PaymentChoice.Installments ->
                mapOf("type" to "installments", "months" to payment.months) + payment.card.toFirestore()
            PaymentChoice.CashOnDelivery -> mapOf("type" to "cod")
        },
        "promoCode" to promoCode,
        "subtotalCents" to totals.subtotalCents,
        "deliveryCents" to totals.deliveryCents,
        "discountCents" to totals.discountCents,
        "totalCents" to totals.totalCents,
        "status" to "placed",
        // serverTimestamp() isn't allowed inside arrays, so the timeline uses the device time.
        "timeline" to listOf(mapOf("status" to "placed", "at" to Timestamp.now())),
        "etaAt" to null,
        "createdAt" to FieldValue.serverTimestamp(),
    )

    private fun PaymentCard.toFirestore() = mapOf("cardId" to id, "brand" to brand.name.lowercase(), "last4" to last4)
}
