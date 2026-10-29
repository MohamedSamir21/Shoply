package com.shopping.shoply.supportchat.domain.rules

/** What a user message is about, by keyword. */
enum class SupportTopic { ORDER, REFUND, DELIVERY, PAYMENT, GENERAL }

// Checked in this order, so "refund my order" is a refund question, not an order one.
private val TOPIC_KEYWORDS = listOf(
    SupportTopic.REFUND to listOf("refund", "return", "money back", "exchange"),
    SupportTopic.PAYMENT to listOf("pay", "card", "charge", "installment", "cash"),
    SupportTopic.DELIVERY to listOf("deliver", "late", "courier", "address", "shipping"),
    SupportTopic.ORDER to listOf("order", "track", "where", "status", "cancel"),
)

/** The first topic whose keywords appear in [message] (any case); [SupportTopic.GENERAL] otherwise. */
fun topicOf(message: String): SupportTopic {
    val text = message.lowercase()
    return TOPIC_KEYWORDS.firstOrNull { (_, words) -> words.any { it in text } }?.first ?: SupportTopic.GENERAL
}

/**
 * The automatic reply for [topic]. There's no live agent yet, so every reply points to where the
 * answer lives in the app and says a person will follow up.
 */
fun cannedReply(topic: SupportTopic): String = when (topic) {
    SupportTopic.ORDER ->
        "You can follow every order live in the Orders tab: tap Track Order for its status and timeline. An agent will follow up here if you need more help."
    SupportTopic.REFUND ->
        "Most items can be returned within 14 days. Open the order in the Orders tab and we'll take it from there. An agent will reply here within a day."
    SupportTopic.DELIVERY ->
        "Sorry about the wait. Delivery times are shown on each order in the Orders tab. An agent is checking with the store and will reply here shortly."
    SupportTopic.PAYMENT ->
        "You can manage your cards in Profile › Payment Methods. You're never charged twice for one order. An agent will look into this and reply here."
    SupportTopic.GENERAL ->
        "Thanks for your message! An agent will reply here shortly. Meanwhile, the Help Center has quick answers to common questions."
}
