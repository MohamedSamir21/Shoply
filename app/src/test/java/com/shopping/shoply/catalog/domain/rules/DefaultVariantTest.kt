package com.shopping.shoply.catalog.domain.rules

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.catalog.domain.model.ColorOption
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Variant
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("DefaultVariantTest")
class DefaultVariantTest {
    private val product = Product(
        id = "1",
        "product1",
        storeId = "store1",
        icon = CatalogIcon.BOOK,
        priceCents = 18,
        oldPriceCents = 20,
        rating = 4.5,
        reviewCount = 19,
        colors = listOf(
            ColorOption("color1", "hex1"),
            ColorOption("color2", "hex2"),
            ColorOption("color3", "hex3")
        ),
        sizes = listOf("size1", "size2", "size3")
    )

    @Test
    @DisplayName("makes a default variant out of a product when there are colors and sizes")
    fun `makes a default variant out of a product when there are colors and sizes`() {
        assertThat(defaultVariant(product)).isEqualTo(
            Variant(
                color = "color1",
                size = "size2"
            )
        )
    }

    @Test
    @DisplayName("makes unknown variant out of a product when there are no colors and no sizes")
    fun `makes unknown variant out of a product when there are no colors and no sizes`() {
        assertThat(
            defaultVariant(
                product.copy(
                    colors = emptyList(),
                    sizes = emptyList()
                )
            )
        ).isEqualTo(
            Variant(null, null)
        )
    }

    @Test
    @DisplayName("makes a default variant out of a product with only one size")
    fun `makes a default variant out of a product with only one size`() {
        assertThat(defaultVariant(product.copy(sizes = listOf("size1")))).isEqualTo(
            Variant(
                color = "color1",
                size = "size1"
            )
        )
    }
}