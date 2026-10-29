package com.shopping.shoply.payments.domain.rules

import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.PaymentCard

enum class CardField { NAME, NUMBER, EXPIRY, CVV }

/** What the Add Card sheet holds. [number] and [cvv] are only validated, never saved. */
data class CardForm(
    val name: String = "",
    val number: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val makeDefault: Boolean = false,
)

/** Visa starts with 4; Mastercard with 51–55 or 2221–2720; anything else is [CardBrand.OTHER]. */
fun cardBrandOf(number: String): CardBrand {
    val digits = number.filter { it.isDigit() }
    val two = digits.take(2).toIntOrNull() ?: 0
    val four = digits.take(4).toIntOrNull() ?: 0
    return when {
        digits.startsWith("4") -> CardBrand.VISA
        two in 51..55 || four in 2221..2720 -> CardBrand.MASTERCARD
        else -> CardBrand.OTHER
    }
}

/** 13–19 digits (spaces allowed) that pass the Luhn checksum. */
fun isValidCardNumber(number: String): Boolean {
    if (number.any { !it.isDigit() && it != ' ' }) return false
    val digits = number.filter { it.isDigit() }
    if (digits.length !in 13..19) return false
    val sum = digits.reversed().mapIndexed { index, char ->
        val digit = char - '0'
        if (index % 2 == 1) (digit * 2).let { if (it > 9) it - 9 else it } else digit
    }.sum()
    return sum % 10 == 0
}

/** "MM/YY" with a real month, not before [nowYear]/[nowMonth] (a card is valid through its expiry month). */
fun isValidExpiry(expiry: String, nowYear: Int, nowMonth: Int): Boolean {
    val match = Regex("""^(\d{2})/(\d{2})$""").matchEntire(expiry.trim()) ?: return false
    val month = match.groupValues[1].toInt()
    val year = 2000 + match.groupValues[2].toInt()
    if (month !in 1..12) return false
    return year > nowYear || (year == nowYear && month >= nowMonth)
}

/** The fields to mark red. CVV is 3 digits, or 4 for cards that aren't Visa/Mastercard. */
fun invalidCardFields(form: CardForm, nowYear: Int, nowMonth: Int): Set<CardField> = buildSet {
    if (form.name.isBlank()) add(CardField.NAME)
    if (!isValidCardNumber(form.number)) add(CardField.NUMBER)
    if (!isValidExpiry(form.expiry, nowYear, nowMonth)) add(CardField.EXPIRY)
    val cvvLengths = if (cardBrandOf(form.number) == CardBrand.OTHER) 3..4 else 3..3
    if (form.cvv.length !in cvvLengths || form.cvv.any { !it.isDigit() }) add(CardField.CVV)
}

/** The card to save: brand, last 4 and expiry only. The first card is always the default. */
fun cardFrom(form: CardForm, isFirst: Boolean) = PaymentCard(
    id = "",
    brand = cardBrandOf(form.number),
    last4 = form.number.filter { it.isDigit() }.takeLast(4),
    expiry = form.expiry.trim(),
    primary = form.makeDefault || isFirst,
)

/** Typing helper: keeps digits and adds the "/" after the month ("0828" → "08/28"). */
fun formatExpiryInput(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(4)
    return if (digits.length <= 2) digits else digits.take(2) + "/" + digits.drop(2)
}

/** Typing helper: digits in groups of 4 ("4242424242424242" → "4242 4242 4242 4242"), at most 19. */
fun formatCardNumberInput(raw: String): String = raw.filter { it.isDigit() }.take(19).chunked(4).joinToString(" ")

/** The other cards that stop being the default when [id] becomes it. */
fun cardIdsToDemote(cards: List<PaymentCard>, id: String): List<String> = cards.filter { it.primary && it.id != id }.map { it.id }

/** The card that becomes the default when [removedId] (the default) is removed; null otherwise. */
fun cardIdToPromote(cards: List<PaymentCard>, removedId: String): String? {
    val removed = cards.firstOrNull { it.id == removedId } ?: return null
    return if (removed.primary) cards.firstOrNull { it.id != removedId }?.id else null
}
