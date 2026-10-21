package com.shopping.shoply.catalog.presentation

import androidx.compose.ui.graphics.vector.ImageVector
import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.circle
import com.shopping.shoply.common.presentation.theme.lineIcon
import com.shopping.shoply.common.presentation.theme.rect

/**
 * The line icon for a catalog glyph (no product photos in v1: a glyph on a soft circle stands in
 * for the image). [CatalogIcon.UNKNOWN] gets the bag icon.
 */
fun CatalogIcon.toImageVector(): ImageVector = when (this) {
    CatalogIcon.DEVICE -> CatalogGlyphs.Device
    CatalogIcon.SHIRT -> CatalogGlyphs.Shirt
    CatalogIcon.SOFA -> CatalogGlyphs.Sofa
    CatalogIcon.SERUM -> CatalogGlyphs.Serum
    CatalogIcon.BALL -> CatalogGlyphs.Ball
    CatalogIcon.APPLE -> CatalogGlyphs.Apple
    CatalogIcon.BOOK -> CatalogGlyphs.Book
    CatalogIcon.BLOCKS -> CatalogGlyphs.Blocks
    CatalogIcon.PLANT -> CatalogGlyphs.Plant
    CatalogIcon.HEADPHONES -> CatalogGlyphs.Headphones
    CatalogIcon.MUG -> CatalogGlyphs.Mug
    CatalogIcon.LAMP -> CatalogGlyphs.Lamp
    CatalogIcon.SNEAKER -> CatalogGlyphs.Sneaker
    CatalogIcon.BACKPACK -> CatalogGlyphs.Backpack
    CatalogIcon.WATCH -> CatalogGlyphs.Watch
    CatalogIcon.THROW -> CatalogGlyphs.Throw
    CatalogIcon.SPEAKER -> CatalogGlyphs.Speaker
    CatalogIcon.MAT -> CatalogGlyphs.Mat
    CatalogIcon.UNKNOWN -> ShoplyAppIcons.Bag
}

/** Product and category glyphs from the Shoply design (app-ui.jsx `PIc`). */
object CatalogGlyphs {
    val Device: ImageVector by lazy { lineIcon("Device", rect(7f, 3f, 10f, 18f, 2.5f), "M10.5 18h3") }
    val Shirt: ImageVector by lazy {
        lineIcon("Shirt", "M8.5 4L4 6.5l1.8 4 2.2-1V20h8V9.5l2.2 1 1.8-4L15.5 4a3.5 3.5 0 0 1 -7 0z")
    }
    val Sofa: ImageVector by lazy {
        lineIcon(
            "Sofa",
            "M5 11V8.5A2.5 2.5 0 0 1 7.5 6h9A2.5 2.5 0 0 1 19 8.5V11" +
                "M3.5 13a2 2 0 0 1 4 0v1.5h9V13a2 2 0 0 1 4 0v4.5h-17zM5.5 17.5V20M18.5 17.5V20",
        )
    }
    val Serum: ImageVector by lazy {
        lineIcon("Serum", "M10 3.5h4v3h-4zM9 6.5h6l1 3v9.5a1.5 1.5 0 0 1 -1.5 1.5h-5A1.5 1.5 0 0 1 8 19V9.5zM8 12h8")
    }
    val Ball: ImageVector by lazy {
        lineIcon(
            "Ball",
            circle(12f, 12f, 8.5f),
            "M5.8 6.2a8.5 8.5 0 0 1 0 11.6M18.2 6.2a8.5 8.5 0 0 0 0 11.6",
        )
    }
    val Apple: ImageVector by lazy {
        lineIcon(
            "Apple",
            "M12 8c-1.5-1-5-1.3-6.3 1.8-1.5 3.6 1 9.7 3.8 10.4 1 0.3 1.6-0.4 2.5-0.4s1.5 0.7 2.5 0.4" +
                "c2.8-0.7 5.3-6.8 3.8-10.4C17 6.7 13.5 7 12 8zM12 8c0-2 1-3.5 2.8-4.3",
        )
    }
    val Book: ImageVector by lazy {
        lineIcon(
            "Book",
            "M5 18.5V5a1.5 1.5 0 0 1 1.5-1.5H19v15H6.5A1.5 1.5 0 0 0 5 20a1.5 1.5 0 0 0 1.5 1.5H19v-3M9 7.5h6",
        )
    }
    val Blocks: ImageVector by lazy {
        lineIcon(
            "Blocks",
            rect(4f, 12.5f, 7f, 7f, 1.5f),
            rect(13f, 12.5f, 7f, 7f, 1.5f),
            rect(8.5f, 4.5f, 7f, 7f, 1.5f),
        )
    }
    val Plant: ImageVector by lazy {
        lineIcon(
            "Plant",
            "M12 20.5v-8M12 12.5c0-3.5 2.5-6 6.5-6 0 3.5-2.5 6-6.5 6zM12 15c0-3-2-5-5.5-5 0 3 2 5 5.5 5zM8 20.5h8",
        )
    }
    val Headphones: ImageVector by lazy {
        lineIcon(
            "Headphones",
            "M5 14v-2a7 7 0 0 1 14 0v2",
            rect(3.5f, 13.5f, 4f, 6.5f, 1.8f),
            rect(16.5f, 13.5f, 4f, 6.5f, 1.8f),
        )
    }
    val Mug: ImageVector by lazy {
        lineIcon(
            "Mug",
            "M5 7.5h11v8a4 4 0 0 1 -4 4H9a4 4 0 0 1 -4 -4z",
            "M16 9.5h1.5a2.5 2.5 0 0 1 0 5H16M9 3.5V5M12 3.5V5",
        )
    }
    val Lamp: ImageVector by lazy { lineIcon("Lamp", "M8 3.5h8l3 7H5zM12 10.5v8M8 20.5h8") }
    val Sneaker: ImageVector by lazy {
        lineIcon(
            "Sneaker",
            "M3.5 16.5V8.5l4 1.2 2.8-2.6 3.4 4.4 4.6 1.3a3 3 0 0 1 2.2 2.9v0.8z",
            "M3.5 19.5h17M10.8 10.6l1.4-1M12.4 12.6l1.4-1",
        )
    }
    val Backpack: ImageVector by lazy {
        lineIcon("Backpack", rect(5.5f, 7f, 13f, 13.5f, 3.5f), "M9 7V5.5a3 3 0 0 1 6 0V7M9 14h6v3.5H9z")
    }
    val Watch: ImageVector by lazy {
        lineIcon(
            "Watch",
            rect(7f, 7f, 10f, 10f, 3f),
            "M9 7l0.8-3.5h4.4L15 7M9 17l0.8 3.5h4.4L15 17M12 9.8V12l1.4 1",
        )
    }
    val Throw: ImageVector by lazy {
        lineIcon("Throw", rect(4f, 5f, 16f, 11f, 2f), "M4 10h16M7 16v3M10 16v3M13 16v3M16 16v3")
    }
    val Speaker: ImageVector by lazy {
        lineIcon("Speaker", rect(6f, 3.5f, 12f, 17f, 3f), circle(12f, 14.5f, 3f), circle(12f, 8f, 1.2f))
    }
    val Mat: ImageVector by lazy {
        lineIcon(
            "Mat",
            circle(7.5f, 12f, 4f),
            "M7.5 8H18a2 2 0 0 1 2 2v4a2 2 0 0 1 -2 2H7.5",
            circle(7.5f, 12f, 1.4f),
        )
    }
}
