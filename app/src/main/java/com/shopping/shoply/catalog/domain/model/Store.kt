package com.shopping.shoply.catalog.domain.model

/**
 * A shop that delivers to the user.
 *
 * @property category display label, e.g. "Home & Living" (not a [Category] id).
 * @property etaMinutes delivery window, e.g. 25..35 shown as "25–35 min".
 * @property deliveryFeeCents 0 means free delivery.
 * @property minOrderCents the smallest order the store accepts; 0 means no minimum.
 * @property opensAtMinutes opening time in minutes since midnight, e.g. 600 for 10:00.
 * @property closesAtMinutes closing time in minutes since midnight, e.g. 1260 for 21:00.
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
    val distanceMiles: Double = 0.0,
    val minOrderCents: Long = 0,
    val opensAtMinutes: Int = 0,
    val closesAtMinutes: Int = MINUTES_PER_DAY,
)

const val MINUTES_PER_DAY = 24 * 60
