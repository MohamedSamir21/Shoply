package com.shopping.shoply.catalog.domain.model

/**
 * A shop that delivers to the user.
 *
 * @property category display label, e.g. "Home & Living" (not a [Category] id).
 * @property etaMinutes delivery window, e.g. 25..35 shown as "25–35 min".
 * @property deliveryFeeCents 0 means free delivery.
 */
data class Store(
    val id: String,
    val name: String,
    val category: String,
    val icon: CatalogIcon,
    val rating: Double,
    val reviewCount: Int,
    val etaMinutes: IntRange,
    val deliveryFeeCents: Long,
)
