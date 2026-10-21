package com.shopping.shoply.catalog.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.ColorOption
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.common.data.firestore.double
import com.shopping.shoply.common.data.firestore.int
import com.shopping.shoply.common.data.firestore.long
import com.shopping.shoply.common.data.firestore.string

// Firestore document -> domain model. Field names match the seed data (Firebase Data/README.md).
// Missing fields fall back to safe defaults instead of crashing.

fun catalogIconOf(name: String?): CatalogIcon =
    CatalogIcon.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CatalogIcon.UNKNOWN

fun DocumentSnapshot.toCategory() = Category(
    id = id,
    name = string("name"),
    icon = catalogIconOf(getString("icon")),
    itemCount = int("itemCount"),
)

fun DocumentSnapshot.toStore() = Store(
    id = id,
    name = string("name"),
    category = string("category"),
    icon = catalogIconOf(getString("icon")),
    rating = double("rating"),
    reviewCount = int("reviewCount"),
    etaMinutes = int("etaMinMinutes")..int("etaMaxMinutes"),
    deliveryFeeCents = long("deliveryFeeCents"),
)

@Suppress("UNCHECKED_CAST")
fun DocumentSnapshot.toProduct() = Product(
    id = id,
    name = string("name"),
    storeId = string("storeId"),
    icon = catalogIconOf(getString("icon")),
    priceCents = long("priceCents"),
    oldPriceCents = getLong("oldPriceCents"),
    rating = double("rating"),
    reviewCount = int("reviewCount"),
    colors = (get("colors") as? List<Map<String, Any?>>).orEmpty().map {
        ColorOption(name = it["name"] as? String ?: "", hex = it["hex"] as? String ?: "")
    },
    sizes = (get("sizes") as? List<String>).orEmpty(),
)
