package com.shopping.shoply.orders.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.cart.domain.rules.CartTotals
import com.shopping.shoply.catalog.data.mapper.catalogIconOf
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.DeliveryOption
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.long
import com.shopping.shoply.orders.domain.OrdersRepository
import com.shopping.shoply.orders.domain.model.Order
import com.shopping.shoply.orders.domain.model.OrderItem
import com.shopping.shoply.orders.domain.model.OrderPayment
import com.shopping.shoply.orders.domain.model.OrderStatus
import com.shopping.shoply.orders.domain.model.TimelineEntry
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Orders in `users/{uid}/orders`, in the schema `FirestoreOrderRepository` writes (see its KDoc).
 * Sorted here, not in the query, so no index is needed. A just-placed order's `createdAt` is
 * still pending on the server, so its local estimate is used.
 */
class FirestoreOrdersRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : OrdersRepository {

    override fun observeOrders(): Flow<List<Order>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return orders(uid).liveSnapshots().map { snapshot ->
            snapshot.documents.map { it.toOrder() }.sortedByDescending { it.createdAtMillis }
        }
    }

    override fun observeOrder(id: String): Flow<Order?> {
        val uid = auth.currentUser?.uid ?: return flowOf(null)
        // A "/" would split the path; no order id has one.
        if (id.isBlank() || '/' in id) return flowOf(null)
        return orders(uid).document(id).liveSnapshots().map { if (it.exists()) it.toOrder() else null }
    }

    private fun orders(uid: String) = firestore.collection("users").document(uid).collection("orders")

    private fun DocumentSnapshot.toOrder(): Order {
        val address = map(get("address"))
        val delivery = map(get("delivery"))
        val payment = map(get("payment"))
        return Order(
            id = id,
            items = list(get("items")).map { item ->
                OrderItem(
                    productId = item.str("productId"),
                    name = item.str("name"),
                    icon = catalogIconOf(item["icon"] as? String),
                    storeId = item.str("storeId"),
                    priceCents = item.num("priceCents"),
                    color = item["color"] as? String,
                    size = item["size"] as? String,
                    quantity = item.num("quantity").toInt(),
                )
            },
            address = DeliveryAddress(
                label = address.str("label"),
                line1 = address.str("line1"),
                id = address.str("id"),
                recipientName = address.str("recipientName"),
                line2 = address.str("line2"),
                phone = address.str("phone"),
            ),
            delivery = if (delivery.str("type") == "express") {
                DeliveryOption.Express
            } else {
                DeliveryOption.Standard(delivery.num("dayStartMillis"), delivery.num("slotStartHour").toInt())
            },
            payment = when (payment.str("type")) {
                "installments" -> OrderPayment.Installments(
                    brandOf(payment.str("brand")),
                    payment.str("last4"),
                    payment.num("months").toInt(),
                )
                "cod" -> OrderPayment.CashOnDelivery
                else -> OrderPayment.Card(brandOf(payment.str("brand")), payment.str("last4"))
            },
            promoCode = getString("promoCode"),
            totals = CartTotals(
                itemCount = list(get("items")).sumOf { it.num("quantity").toInt() },
                subtotalCents = long("subtotalCents"),
                deliveryCents = long("deliveryCents"),
                discountCents = long("discountCents"),
                totalCents = long("totalCents"),
            ),
            status = statusOf(getString("status")),
            timeline = list(get("timeline")).map { entry ->
                TimelineEntry(
                    status = statusOf(entry["status"] as? String),
                    atMillis = (entry["at"] as? Timestamp)?.toDate()?.time ?: 0L,
                    note = entry["note"] as? String,
                )
            },
            etaAtMillis = getTimestamp("etaAt")?.toDate()?.time,
            createdAtMillis = getTimestamp("createdAt", ServerTimestampBehavior.ESTIMATE)?.toDate()?.time ?: 0L,
        )
    }

    private fun statusOf(name: String?) =
        OrderStatus.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OrderStatus.PLACED

    private fun brandOf(name: String) = when (name.lowercase()) {
        "visa" -> CardBrand.VISA
        "mastercard" -> CardBrand.MASTERCARD
        else -> CardBrand.OTHER
    }

    @Suppress("UNCHECKED_CAST")
    private fun map(value: Any?): Map<String, Any?> = value as? Map<String, Any?> ?: emptyMap()

    private fun list(value: Any?): List<Map<String, Any?>> = (value as? List<*>).orEmpty().map(::map)

    private fun Map<String, Any?>.str(key: String) = this[key] as? String ?: ""

    private fun Map<String, Any?>.num(key: String) = (this[key] as? Number)?.toLong() ?: 0L
}
