package com.shopping.shoply.orders.presentation.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shopping.shoply.R
import com.shopping.shoply.checkout.domain.model.DeliveryOption
import com.shopping.shoply.checkout.presentation.components.brandName
import com.shopping.shoply.checkout.presentation.components.slotLabel
import com.shopping.shoply.orders.domain.model.Order
import com.shopping.shoply.orders.domain.model.OrderPayment
import com.shopping.shoply.orders.domain.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Text for order dates, statuses, delivery and payment. Dates use US English, like the rest of the app.

/** "Oct 18 · 1:02 PM". */
internal fun dateTimeLabel(millis: Long): String = SimpleDateFormat("MMM d · h:mm a", Locale.US).format(Date(millis))

/** "1:46 PM". */
internal fun timeLabel(millis: Long): String = SimpleDateFormat("h:mm a", Locale.US).format(Date(millis))

@StringRes
internal fun OrderStatus.labelRes(): Int = when (this) {
    OrderStatus.PLACED -> R.string.orders_status_placed
    OrderStatus.CONFIRMED -> R.string.orders_status_confirmed
    OrderStatus.PACKED -> R.string.orders_status_packed
    OrderStatus.ON_THE_WAY -> R.string.orders_status_on_the_way
    OrderStatus.DELIVERED -> R.string.orders_status_delivered
    OrderStatus.CANCELLED -> R.string.orders_status_cancelled
}

/** "Arriving by 1:46 PM", or when it's due: "Delivery Wed, Oct 28 · 2 – 4 PM" / "Express · within 45 min". */
@Composable
internal fun arrivalLabel(order: Order): String {
    order.etaAtMillis?.let { return stringResource(R.string.orders_arriving_by, timeLabel(it)) }
    return when (val delivery = order.delivery) {
        DeliveryOption.Express -> stringResource(R.string.checkout_express_label)
        is DeliveryOption.Standard -> stringResource(R.string.orders_delivery_on, standardLabel(delivery))
    }
}

/** "Wed, Oct 28 · 2 – 4 PM". */
internal fun standardLabel(delivery: DeliveryOption.Standard): String =
    SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(delivery.dayStartMillis)) + " · " + slotLabel(delivery.slotStartHour)

/** "Visa •••• 4821", "Pay in 3 · Visa •••• 4821" or "Cash on delivery". */
@Composable
internal fun paymentLabel(payment: OrderPayment): String = when (payment) {
    is OrderPayment.Card -> stringResource(R.string.checkout_card_title, brandName(payment.brand), payment.last4)
    is OrderPayment.Installments -> stringResource(
        R.string.checkout_installments_label,
        payment.months,
        stringResource(R.string.checkout_card_title, brandName(payment.brand), payment.last4),
    )
    OrderPayment.CashOnDelivery -> stringResource(R.string.checkout_cod_label)
}
