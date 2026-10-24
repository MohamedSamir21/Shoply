package com.shopping.shoply.product.presentation.preview

import com.shopping.shoply.catalog.domain.model.ColorOption
import com.shopping.shoply.home.presentation.preview.HomePreviewData

/** Sample products for the Product Details previews only. */
internal object ProductPreviewData {
    val headphones = HomePreviewData.headphones.copy(
        colors = listOf(ColorOption("Sand", "#D8CAB6"), ColorOption("Charcoal", "#3A3735"), ColorOption("Sage", "#A2B09A")),
        sizes = listOf("S", "M", "L"),
        description = "Soft memory-foam cushions, up to 40 hours of battery and active noise cancelling for " +
            "calm listening at home or on the go. Folds flat into the included travel case.",
    )
    val mug = HomePreviewData.mug.copy(description = "Hand-glazed stoneware mug that keeps coffee warm a little longer.")
    val store = HomePreviewData.voltElectronics
    val similar = listOf(HomePreviewData.linenShirt, HomePreviewData.mug, HomePreviewData.lamp)
}
