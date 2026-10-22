package com.shopping.shoply.help.domain.rules

import com.shopping.shoply.help.domain.model.Faq

/**
 * The FAQs matching the Help Center search.
 *
 * @return all [faqs] when [query] is blank; otherwise those whose question or answer contains the
 *   trimmed [query], ignoring case, in their original order.
 */
fun filterFaqs(faqs: List<Faq>, query: String): List<Faq> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return faqs
    return faqs.filter {
        it.question.contains(trimmed, ignoreCase = true) || it.answer.contains(trimmed, ignoreCase = true)
    }
}
