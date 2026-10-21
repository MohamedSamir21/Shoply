package com.shopping.shoply.catalog.domain.model

/**
 * A product a store sells. Money is always in cents to avoid floating-point rounding.
 *
 * @property oldPriceCents the price before a discount; null when the product isn't discounted.
 * @property colors available colours in display order; may be empty.
 * @property sizes available sizes in display order (e.g. S, M, L); may be empty.
 */
data class Product(
    val id: String,
    val name: String,
    val storeId: String,
    val icon: CatalogIcon,
    val priceCents: Long,
    val oldPriceCents: Long?,
    val rating: Double,
    val reviewCount: Int,
    val colors: List<ColorOption>,
    val sizes: List<String>,
)

/** A colour choice. [hex] is "#RRGGBB", used for the swatch. */
data class ColorOption(val name: String, val hex: String)

/** One concrete choice of a product: which colour and size. Null when the product has none. */
data class Variant(val color: String?, val size: String?)
