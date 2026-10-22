package com.shopping.shoply.testutil

import com.shopping.shoply.help.domain.model.Faq

// Test data builders for the Help Center, same idea as CatalogBuilders.
//   aFaq(question = "Where is my order?")      one FAQ
//   faqs(3)                                     many FAQs with ids f0..f2

fun aFaq(
    id: String = "f1",
    question: String = "Question $id",
    answer: String = "Answer $id",
) = Faq(id, question, answer)

fun faqs(count: Int) = List(count) { aFaq(id = "f$it") }

// A fixed list for filterFaqs tests. Each FAQ is built so one query hits one case:
//   "order"   → f1 (question) and f3 (answer only), in that order
//   "REFUND"  → f2 (case ignored)
//   "cash"    → f4 only
//   "crypto"  → nothing
val sampleFaqs = listOf(
    aFaq(id = "f1", question = "Where is my order?", answer = "Track it from the Orders tab."),
    aFaq(id = "f2", question = "How do refunds work?", answer = "Money is back within 5 days."),
    aFaq(id = "f3", question = "Can I change my address?", answer = "Yes, until the store confirms your order."),
    aFaq(id = "f4", question = "Which payment methods can I use?", answer = "Cards and cash on delivery."),
)
