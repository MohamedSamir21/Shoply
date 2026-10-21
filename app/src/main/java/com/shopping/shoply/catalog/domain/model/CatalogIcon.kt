package com.shopping.shoply.catalog.domain.model

/**
 * The line-icon a category, store or product is drawn with. There are no product photos in v1;
 * the UI maps each value to a glyph on a soft circle.
 *
 * [UNKNOWN] is the fallback for an icon name the app doesn't know yet (e.g. new data in Firestore),
 * so old app versions still render instead of crashing.
 */
enum class CatalogIcon {
    DEVICE, SHIRT, SOFA, SERUM, BALL, APPLE, BOOK, BLOCKS, PLANT,
    HEADPHONES, MUG, LAMP, SNEAKER, BACKPACK, WATCH, THROW, SPEAKER, MAT,
    UNKNOWN,
}
