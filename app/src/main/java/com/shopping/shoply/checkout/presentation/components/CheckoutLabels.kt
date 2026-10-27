package com.shopping.shoply.checkout.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shopping.shoply.R
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.checkout.domain.model.PaymentChoice
import com.shopping.shoply.checkout.domain.rules.DeliveryDay
import com.shopping.shoply.checkout.presentation.CheckoutState
import com.shopping.shoply.checkout.presentation.DeliverySpeed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Text for the checkout's days, slots, cards and payment choices. Dates use US English to match
// the rest of the app's copy.

/** A 2-hour window: 10 → "10 AM – 12 PM", 12 → "12 – 2 PM", 20 → "8 – 10 PM". */
internal fun slotLabel(startHour: Int): String {
    val end = startHour + 2
    val startPeriod = period(startHour)
    val endPeriod = period(end)
    return if (startPeriod == endPeriod) {
        "${hour12(startHour)} – ${hour12(end)} $endPeriod"
    } else {
        "${hour12(startHour)} $startPeriod – ${hour12(end)} $endPeriod"
    }
}

private fun hour12(hour: Int) = (hour % 12).let { if (it == 0) 12 else it }
private fun period(hour: Int) = if (hour % 24 < 12) "AM" else "PM"

/** "Today" for today's date, else the short weekday ("Wed"). */
@Composable
internal fun dayName(day: DeliveryDay): String {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return if (day.startMillis == today) {
        stringResource(R.string.checkout_today)
    } else {
        SimpleDateFormat("EEE", Locale.US).format(Date(day.startMillis))
    }
}

/** "Oct 27". */
internal fun dayDate(day: DeliveryDay): String = SimpleDateFormat("MMM d", Locale.US).format(Date(day.startMillis))

internal fun brandName(brand: CardBrand) = when (brand) {
    CardBrand.VISA -> "Visa"
    CardBrand.MASTERCARD -> "Mastercard"
    CardBrand.OTHER -> "Card"
}

/** "Visa •••• 4821". */
@Composable
internal fun cardLabel(card: PaymentCard) = stringResource(R.string.checkout_card_title, brandName(card.brand), card.last4)

/** "Visa •••• 4821", "Pay in 3 · Visa •••• 4821" or "Cash on delivery". */
@Composable
internal fun paymentLabel(payment: PaymentChoice): String = when (payment) {
    is PaymentChoice.Card -> cardLabel(payment.card)
    is PaymentChoice.Installments -> stringResource(R.string.checkout_installments_label, payment.months, cardLabel(payment.card))
    PaymentChoice.CashOnDelivery -> stringResource(R.string.checkout_cod_label)
}

/** "Wed, Oct 28 · 2 – 4 PM" or "Express · within 45 min". */
@Composable
internal fun deliveryLabel(state: CheckoutState): String {
    if (state.speed == DeliverySpeed.EXPRESS) return stringResource(R.string.checkout_express_label)
    val day = state.days.getOrNull(state.dayIndex) ?: return ""
    val slot = state.slotHour?.let { " · " + slotLabel(it) }.orEmpty()
    return "${dayName(day)}, ${dayDate(day)}$slot"
}
