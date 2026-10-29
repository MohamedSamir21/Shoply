package com.shopping.shoply.store.presentation.preview

import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.home.presentation.preview.HomePreviewData
import com.shopping.shoply.store.domain.rules.OpeningStatus
import com.shopping.shoply.store.presentation.StoreBody
import com.shopping.shoply.store.presentation.StoreState

/** Sample store for previews, like the seeded `stores/atelier`. */
internal object StorePreviewData {
    val atelier = Store(
        id = "atelier",
        name = "Atelier Home",
        category = "Home & Living",
        icon = CatalogIcon.SOFA,
        rating = 4.7,
        reviewCount = 864,
        etaMinutes = 35..45,
        deliveryFeeCents = 249,
        distanceMiles = 2.4,
        minOrderCents = 2000,
        opensAtMinutes = 10 * 60,
        closesAtMinutes = 21 * 60,
    )

    val products = listOf(HomePreviewData.lamp, HomePreviewData.mug, HomePreviewData.headphones)

    fun state(body: StoreBody) = StoreState(
        body = body,
        opening = OpeningStatus.OpenUntil(atelier.closesAtMinutes),
        favorite = true,
        wishlist = setOf(HomePreviewData.mug.id),
    )
}
