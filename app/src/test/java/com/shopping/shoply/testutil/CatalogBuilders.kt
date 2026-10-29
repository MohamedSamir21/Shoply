package com.shopping.shoply.testutil

import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.ColorOption
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.home.domain.model.HomeFeed
import com.shopping.shoply.home.domain.model.Promo
import com.shopping.shoply.home.domain.model.PromoArt
import com.shopping.shoply.home.domain.model.PromoTarget

// Test data builders: every field has a valid default, so a test only names what it cares about.
//   aProduct(oldPriceCents = 10900)            one object
//   products(12)                                many objects with ids p0..p11
//   aHomeFeed(promos = promos(5))               a feed with 5 promos, defaults for the rest

fun aProduct(
    id: String = "p1",
    name: String = "Product $id",
    storeId: String = "store1",
    icon: CatalogIcon = CatalogIcon.HEADPHONES,
    priceCents: Long = 8900,
    oldPriceCents: Long? = null,
    rating: Double = 4.5,
    reviewCount: Int = 10,
    colors: List<ColorOption> = listOf(ColorOption("Sand", "#D8CAB6"), ColorOption("Charcoal", "#3A3735")),
    sizes: List<String> = listOf("S", "M", "L"),
    description: String = "About $id",
    categoryId: String = "electronics",
    subcategory: String = "",
    keywords: String = "",
) = Product(id, name, storeId, icon, priceCents, oldPriceCents, rating, reviewCount, colors, sizes, description, categoryId, subcategory, keywords)

fun aStore(
    id: String = "store1",
    name: String = "Store $id",
    category: String = "Electronics",
    icon: CatalogIcon = CatalogIcon.DEVICE,
    rating: Double = 4.8,
    reviewCount: Int = 100,
    etaMinutes: IntRange = 25..35,
    deliveryFeeCents: Long = 199,
    opensAtMinutes: Int = 10 * 60,
    closesAtMinutes: Int = 21 * 60,
    distanceMiles: Double = 1.0,
) = Store(
    id, name, category, icon, rating, reviewCount, etaMinutes, deliveryFeeCents,
    distanceMiles = distanceMiles, opensAtMinutes = opensAtMinutes, closesAtMinutes = closesAtMinutes,
)

fun aCategory(
    id: String = "electronics",
    name: String = "Category $id",
    icon: CatalogIcon = CatalogIcon.DEVICE,
    itemCount: Int = 7,
) = Category(id, name, icon, itemCount)

fun aPromo(
    id: String = "promo1",
    kicker: String = "This week",
    title: String = "Promo $id",
    ctaLabel: String = "Shop Now",
    art: PromoArt = PromoArt.TRUCK,
    target: PromoTarget = PromoTarget.CategoryListing("home"),
) = Promo(id, kicker, title, ctaLabel, art, target)

fun products(count: Int) = List(count) { aProduct(id = "p$it") }
fun stores(count: Int) = List(count) { aStore(id = "store$it") }
fun categories(count: Int) = List(count) { aCategory(id = "category$it") }
fun promos(count: Int) = List(count) { aPromo(id = "promo$it") }

/** A small valid feed: every section has fewer items than its cap, so nothing gets trimmed. */
fun aHomeFeed(
    promos: List<Promo> = promos(2),
    categories: List<Category> = categories(3),
    featuredStores: List<Store> = stores(2),
    popular: List<Product> = products(3),
    recommended: List<Product> = products(4),
) = HomeFeed(promos, categories, featuredStores, popular, recommended)
