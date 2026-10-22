package com.shopping.shoply.help.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shopping.shoply.R
import com.shopping.shoply.help.domain.model.Faq

/** A Help Center question as string resources, resolved to a [Faq] in the current language. */
data class FaqRes(val id: String, @param:StringRes val question: Int, @param:StringRes val answer: Int)

/** The Help Center's questions, in display order. */
val helpFaqs: List<FaqRes> = listOf(
    FaqRes("order-status", R.string.help_faq_order_status_q, R.string.help_faq_order_status_a),
    FaqRes("installments", R.string.help_faq_installments_q, R.string.help_faq_installments_a),
    FaqRes("returns", R.string.help_faq_returns_q, R.string.help_faq_returns_a),
    FaqRes("change-address", R.string.help_faq_change_address_q, R.string.help_faq_change_address_a),
    FaqRes("payment-methods", R.string.help_faq_payment_methods_q, R.string.help_faq_payment_methods_a),
)

/** [helpFaqs] in the current language. */
@Composable
fun localizedHelpFaqs(): List<Faq> = helpFaqs.map {
    Faq(id = it.id, question = stringResource(it.question), answer = stringResource(it.answer))
}
