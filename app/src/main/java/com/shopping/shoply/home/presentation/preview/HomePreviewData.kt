package com.shopping.shoply.home.presentation.preview

import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.ColorOption
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.home.domain.model.HomeFeed
import com.shopping.shoply.home.domain.model.Promo
import com.shopping.shoply.home.domain.model.PromoArt
import com.shopping.shoply.home.domain.model.PromoTarget
import com.shopping.shoply.home.domain.rules.Greeting
import com.shopping.shoply.home.presentation.HomeBody
import com.shopping.shoply.home.presentation.HomeState

/**
 * Sample data for Home's `@Preview`s only (the test builders live in the test source set, which
 * previews can't see). Values match the design's demo content.
 */
internal object HomePreviewData {

    val headphones = Product(
        id = "p1",
        name = "Wireless Noise-Cancelling Headphones",
        storeId = "volt",
        icon = CatalogIcon.HEADPHONES,
        priceCents = 8900,
        oldPriceCents = 10900,
        rating = 4.6,
        reviewCount = 128,
        colors = listOf(ColorOption("Charcoal", "#2B2B2B")),
        sizes = emptyList(),
    )

    val linenShirt = Product(
        id = "p2",
        name = "Linen Shirt",
        storeId = "thread",
        icon = CatalogIcon.SHIRT,
        priceCents = 3450,
        oldPriceCents = null,
        rating = 4.4,
        reviewCount = 56,
        colors = listOf(ColorOption("Sand", "#D8C3A5"), ColorOption("Charcoal", "#2B2B2B")),
        sizes = listOf("S", "M", "L"),
    )

    val mug = Product(
        id = "p3",
        name = "Stoneware Mug",
        storeId = "nest",
        icon = CatalogIcon.MUG,
        priceCents = 1800,
        oldPriceCents = null,
        rating = 4.8,
        reviewCount = 212,
        colors = emptyList(),
        sizes = emptyList(),
    )

    val lamp = Product(
        id = "p4",
        name = "Arc Desk Lamp",
        storeId = "nest",
        icon = CatalogIcon.LAMP,
        priceCents = 4900,
        oldPriceCents = 6500,
        rating = 4.5,
        reviewCount = 87,
        colors = emptyList(),
        sizes = emptyList(),
    )

    val fernAndCo = Store(
        id = "fern",
        name = "Fern & Co.",
        category = "Plants",
        icon = CatalogIcon.PLANT,
        rating = 4.9,
        reviewCount = 410,
        etaMinutes = 30..45,
        deliveryFeeCents = 299,
    )

    val voltElectronics = Store(
        id = "volt",
        name = "Volt Electronics",
        category = "Electronics",
        icon = CatalogIcon.DEVICE,
        rating = 4.8,
        reviewCount = 1240,
        etaMinutes = 25..35,
        deliveryFeeCents = 0,
    )

    val promos = listOf(
        Promo(
            id = "free-delivery",
            kicker = "This week",
            title = "Free delivery on orders over \$35",
            ctaLabel = "Shop Now",
            art = PromoArt.TRUCK,
            target = PromoTarget.CategoryListing("home"),
        ),
        Promo(
            id = "split-pay",
            kicker = "Flexible payment",
            title = "Split any order into 3 payments",
            ctaLabel = "Learn More",
            art = PromoArt.SPLIT,
            target = PromoTarget.Help,
        ),
        Promo(
            id = "fern",
            kicker = "New store",
            title = "Indoor plants from Fern & Co.",
            ctaLabel = "Visit Store",
            art = PromoArt.PLANT,
            target = PromoTarget.Store("fern"),
        ),
    )

    /** Header state for a signed-in user with an address and unread notifications. */
    val headerState = HomeState(
        greeting = Greeting.MORNING,
        firstName = "Mohamed",
        initials = "MS",
        hasUnread = true,
        addressLine = "Home · 24 Elm Street",
    )

    /** Header state for a user with no name and no address yet. */
    val blankHeaderState = HomeState(
        greeting = Greeting.EVENING,
        firstName = "there",
        initials = null,
        hasUnread = false,
        addressLine = null,
    )

    val categories = listOf(
        Category("electronics", "Electronics", CatalogIcon.DEVICE, 128),
        Category("fashion", "Fashion", CatalogIcon.SHIRT, 342),
        Category("home", "Home", CatalogIcon.SOFA, 96),
        Category("beauty", "Beauty", CatalogIcon.SERUM, 74),
        Category("sports", "Sports", CatalogIcon.BALL, 58),
    )

    val feed = HomeFeed(
        promos = promos,
        categories = categories,
        featuredStores = listOf(voltElectronics, fernAndCo),
        popular = listOf(headphones, lamp, mug, linenShirt),
        recommended = listOf(linenShirt, mug, headphones, lamp, mug.copy(id = "p5")),
    )

    /** A whole Home screen state for a given body; the header is the signed-in one. */
    fun state(body: HomeBody) = headerState.copy(body = body, wishlist = setOf(headphones.id))
}
