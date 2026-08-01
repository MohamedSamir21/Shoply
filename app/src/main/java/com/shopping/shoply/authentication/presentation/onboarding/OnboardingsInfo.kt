package com.shopping.shoply.authentication.presentation.onboarding

import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.utility.UiText

fun generateOnboardings(): List<Onboarding> {
    return listOf(
        Onboarding(
            image = R.drawable.shopping_illustration,
            title = UiText.StringResource(R.string.shop_from_your_favorite_store),
            description = UiText.StringResource(R.string.discover_a_world_of_convenience_and_endless_choices)
        ),
        Onboarding(
            image = R.drawable.delivery_illustration,
            title = UiText.StringResource(R.string.get_it_delivered),
            description = UiText.StringResource(R.string.get_a_fast_and_reliable_delivery_service)
        ),
        Onboarding(
            image = R.drawable.payment_illustration,
            title = UiText.StringResource(R.string.flexible_payment),
            description = UiText.StringResource(R.string.pay_in_versatile_and_secure_payment_methods)
        )
    )
}
