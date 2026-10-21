package com.shopping.shoply.home.data.mapper

import com.google.firebase.firestore.DocumentSnapshot
import com.shopping.shoply.common.data.firestore.string
import com.shopping.shoply.home.domain.model.Promo
import com.shopping.shoply.home.domain.model.PromoArt
import com.shopping.shoply.home.domain.model.PromoTarget

/** @return the promo, or null when its target type is unknown (that banner is skipped). */
fun DocumentSnapshot.toPromo(): Promo? {
    val target = when (getString("target.type")) {
        "category" -> getString("target.id")?.let { PromoTarget.CategoryListing(it) }
        "store" -> getString("target.id")?.let { PromoTarget.Store(it) }
        "help" -> PromoTarget.Help
        else -> null
    } ?: return null
    return Promo(
        id = id,
        kicker = string("kicker"),
        title = string("title"),
        ctaLabel = string("ctaLabel"),
        art = PromoArt.entries.firstOrNull { it.name.equals(getString("illustration"), ignoreCase = true) }
            ?: PromoArt.UNKNOWN,
        target = target,
    )
}
