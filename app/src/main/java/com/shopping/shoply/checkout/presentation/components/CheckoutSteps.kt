package com.shopping.shoply.checkout.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.cart.domain.rules.EXPRESS_FEE_CENTS
import com.shopping.shoply.cart.presentation.components.PriceSummary
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.checkout.domain.model.PaymentChoice
import com.shopping.shoply.checkout.presentation.CheckoutIntent
import com.shopping.shoply.checkout.presentation.CheckoutState
import com.shopping.shoply.checkout.presentation.DeliverySpeed
import com.shopping.shoply.checkout.presentation.STEP_ADDRESS
import com.shopping.shoply.checkout.presentation.STEP_DELIVERY
import com.shopping.shoply.checkout.presentation.STEP_PAYMENT
import com.shopping.shoply.common.presentation.components.SelectCard
import com.shopping.shoply.common.presentation.components.SelectCardBadge
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.darkRing
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.common.presentation.util.formatPrice
import com.shopping.shoply.home.presentation.components.ProductImage
import com.shopping.shoply.profile.domain.model.DeliveryAddress

/** Step 1: saved addresses as radio cards, "Add New Address", and the contact line. */
@Composable
fun AddressStep(state: CheckoutState, onIntent: (CheckoutIntent) -> Unit, onAddAddress: () -> Unit) {
    SectionTitle(stringResource(R.string.checkout_address_title), first = true)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.addresses.forEach { address ->
            SelectCard(
                title = address.label,
                subtitle = listOf(address.line1, address.line2).filter { it.isNotBlank() }.joinToString("\n"),
                icon = if (address.label.equals("home", ignoreCase = true)) ShoplyAppIcons.Home else ShoplyAppIcons.Pin,
                badge = if (address.primary) {
                    { SelectCardBadge(stringResource(R.string.checkout_address_default)) }
                } else {
                    null
                },
                selected = address.id == state.selectedAddressId,
                onClick = { onIntent(CheckoutIntent.SelectAddress(address.id)) },
            )
        }
        DashedAddButton(stringResource(R.string.checkout_add_address), onAddAddress)
    }
    state.selectedAddress?.let { address ->
        SectionTitle(stringResource(R.string.checkout_contact_title))
        ContactRow(address)
    }
}

@Composable
private fun ContactRow(address: DeliveryAddress) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shoplyCard()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(ShoplyAppIcons.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(
            text = listOf(address.recipientName, address.phone).filter { it.isNotBlank() }.joinToString(" · "),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Step 2: Standard (day + slot pickers) or Express. */
@Composable
fun DeliveryStep(state: CheckoutState, onIntent: (CheckoutIntent) -> Unit) {
    val express = state.speed == DeliverySpeed.EXPRESS
    val standardFee = state.totals.deliveryCents - if (express) EXPRESS_FEE_CENTS else 0
    SectionTitle(stringResource(R.string.checkout_speed_title), first = true)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectCard(
            title = stringResource(R.string.checkout_standard),
            subtitle = stringResource(R.string.checkout_standard_sub),
            icon = ShoplyAppIcons.Truck,
            trailing = {
                if (standardFee == 0L) {
                    TrailingText(stringResource(R.string.cart_delivery_free), MaterialTheme.shoplyExtra.onGreenSoft)
                } else {
                    TrailingText(formatPrice(standardFee))
                }
            },
            selected = !express,
            onClick = { onIntent(CheckoutIntent.SelectSpeed(DeliverySpeed.STANDARD)) },
        )
        SelectCard(
            title = stringResource(R.string.checkout_express),
            subtitle = stringResource(R.string.checkout_express_sub),
            icon = ShoplyAppIcons.Bolt,
            trailing = { TrailingText(stringResource(R.string.checkout_express_fee, formatPrice(EXPRESS_FEE_CENTS))) },
            selected = express,
            onClick = { onIntent(CheckoutIntent.SelectSpeed(DeliverySpeed.EXPRESS)) },
        )
    }
    if (express) return

    SectionTitle(stringResource(R.string.checkout_day_title))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        state.days.forEachIndexed { index, day ->
            val selected = index == state.dayIndex
            PickerBox(
                selected = selected,
                onClick = { onIntent(CheckoutIntent.SelectDay(index)) },
                modifier = Modifier.weight(1f).height(64.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = dayName(day),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = dayDate(day),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    SectionTitle(stringResource(R.string.checkout_time_title))
    val slots = state.days.getOrNull(state.dayIndex)?.slots.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        slots.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { slot ->
                    val selected = !slot.full && slot.startHour == state.slotHour
                    PickerBox(
                        selected = selected,
                        enabled = !slot.full,
                        onClick = { onIntent(CheckoutIntent.SelectSlot(slot.startHour)) },
                        modifier = Modifier.weight(1f).height(46.dp),
                    ) {
                        Text(
                            text = slotLabel(slot.startHour),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (slot.full) TextDecoration.LineThrough else null,
                            color = when {
                                slot.full -> MaterialTheme.shoplyExtra.inkFaint
                                selected -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Step 3: saved cards, installments (3 or 6 months, on the picked card), cash on delivery. */
@Composable
fun PaymentStep(state: CheckoutState, onIntent: (CheckoutIntent) -> Unit, onAddCard: () -> Unit) {
    val payment = state.payment
    val installmentCard = when (payment) {
        is PaymentChoice.Card -> payment.card
        is PaymentChoice.Installments -> payment.card
        else -> state.cards.firstOrNull()
    }
    SectionTitle(stringResource(R.string.checkout_payment_title), first = true)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.cards.forEach { card ->
            SelectCard(
                title = cardLabel(card),
                subtitle = stringResource(R.string.checkout_card_expires, card.expiry),
                lead = { CardBrandBadge(card.brand) },
                selected = payment == PaymentChoice.Card(card),
                onClick = { onIntent(CheckoutIntent.SelectPayment(PaymentChoice.Card(card))) },
            )
        }
        if (installmentCard != null) {
            val months = (payment as? PaymentChoice.Installments)?.months ?: 3
            SelectCard(
                title = stringResource(R.string.checkout_installments),
                subtitle = stringResource(R.string.checkout_installments_sub),
                icon = ShoplyAppIcons.Split,
                selected = payment is PaymentChoice.Installments,
                onClick = { onIntent(CheckoutIntent.SelectPayment(PaymentChoice.Installments(installmentCard, months))) },
                expanded = {
                    InstallmentPlan(
                        card = installmentCard,
                        months = months,
                        totalCents = state.totals.totalCents,
                        onMonths = { onIntent(CheckoutIntent.SelectPayment(PaymentChoice.Installments(installmentCard, it))) },
                    )
                },
            )
        }
        SelectCard(
            title = stringResource(R.string.checkout_cod),
            subtitle = stringResource(R.string.checkout_cod_sub),
            icon = ShoplyAppIcons.Cash,
            selected = payment == PaymentChoice.CashOnDelivery,
            onClick = { onIntent(CheckoutIntent.SelectPayment(PaymentChoice.CashOnDelivery)) },
        )
        DashedAddButton(stringResource(R.string.checkout_add_card), onAddCard)
    }
}

@Composable
private fun InstallmentPlan(card: PaymentCard, months: Int, totalCents: Long, onMonths: (Int) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val perMonth = (totalCents + months - 1) / months
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(3, 6).forEach { option ->
            val selected = option == months
            Text(
                text = stringResource(R.string.checkout_months, option),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) scheme.onPrimaryContainer else scheme.onSurface,
                modifier = Modifier
                    .clip(ShoplyShapes.pill)
                    .background(if (selected) scheme.primaryContainer else scheme.surface)
                    .border(if (selected) 2.dp else 1.4.dp, if (selected) scheme.primary else scheme.outlineVariant, ShoplyShapes.pill)
                    .selectable(selected = selected, onClick = { onMonths(option) }, role = Role.RadioButton)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
    }
    Column(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .background(scheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PlanRow(stringResource(R.string.checkout_due_today), formatPrice(perMonth))
        PlanRow(stringResource(R.string.checkout_then_monthly, months - 1), formatPrice(perMonth))
        PlanRow(stringResource(R.string.checkout_interest), formatPrice(0), MaterialTheme.shoplyExtra.onGreenSoft)
    }
    Text(
        text = stringResource(R.string.checkout_charged_to, cardLabel(card)),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = scheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 10.dp),
    )
}

@Composable
private fun PlanRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row {
        Text(label, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

/** Step 4: what was picked (each with Change), the items, the price summary and the terms line. */
@Composable
fun ReviewStep(state: CheckoutState, onIntent: (CheckoutIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.selectedAddress?.let { address ->
            ReviewBlock(
                title = stringResource(R.string.checkout_deliver_to),
                icon = ShoplyAppIcons.Pin,
                text = "${address.label} · ${address.line1}",
                detail = address.line2.ifBlank { null },
                onChange = { onIntent(CheckoutIntent.GoToStep(STEP_ADDRESS)) },
            )
        }
        ReviewBlock(
            title = stringResource(R.string.checkout_review_delivery),
            icon = ShoplyAppIcons.Clock,
            text = deliveryLabel(state),
            onChange = { onIntent(CheckoutIntent.GoToStep(STEP_DELIVERY)) },
        )
        state.payment?.let { payment ->
            ReviewBlock(
                title = stringResource(R.string.checkout_review_payment),
                icon = when (payment) {
                    is PaymentChoice.Card -> ShoplyAppIcons.Card
                    is PaymentChoice.Installments -> ShoplyAppIcons.Split
                    PaymentChoice.CashOnDelivery -> ShoplyAppIcons.Cash
                },
                text = paymentLabel(payment),
                onChange = { onIntent(CheckoutIntent.GoToStep(STEP_PAYMENT)) },
            )
        }
    }
    SectionTitle(stringResource(R.string.checkout_items_title, state.totals.itemCount))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shoplyCard()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        state.items.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProductImage(icon = item.icon, modifier = Modifier.size(48.dp), cornerRadius = 10.dp, circleRatio = 0.72f)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = listOfNotNull(item.color, item.size, "× ${item.quantity}").joinToString(" · "),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Text(
                    text = formatPrice(item.priceCents * item.quantity),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
    PriceSummary(totals = state.totals, promoCode = state.promo?.code, modifier = Modifier.padding(top = 14.dp))
    Text(
        text = stringResource(R.string.checkout_terms),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 19.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 14.dp),
    )
}

@Composable
private fun ReviewBlock(title: String, icon: ImageVector, text: String, onChange: () -> Unit, detail: String? = null) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shoplyCard()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(scheme.primaryContainer, RoundedCornerShape(12.dp))
                .darkRing(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.96.sp,
                color = scheme.onSurfaceVariant,
            )
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 21.sp,
                color = scheme.onSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (detail != null) {
                Text(text = detail, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = scheme.onSurfaceVariant)
            }
        }
        Text(
            text = stringResource(R.string.checkout_change),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onChange)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        )
    }
}

/** The card brand mark in a white square: italic "VISA" or the Mastercard circles. */
@Composable
private fun CardBrandBadge(brand: CardBrand) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        when (brand) {
            CardBrand.VISA -> Text(
                text = "VISA",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                color = Color(0xFF1A1F71),
            )
            CardBrand.MASTERCARD -> Box(
                modifier = Modifier
                    .size(width = 28.dp, height = 18.dp)
                    .drawBehind {
                        val r = size.height / 2
                        drawCircle(Color(0xFFEB001B), r, center.copy(x = r))
                        drawCircle(Color(0xFFF79E1B).copy(alpha = 0.9f), r, center.copy(x = size.width - r))
                    },
            )
            CardBrand.OTHER -> Icon(ShoplyAppIcons.Card, contentDescription = null, tint = Color(0xFF1A1F71))
        }
    }
}

@Composable
private fun PickerBox(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) scheme.primaryContainer else scheme.surface)
            .border(if (selected) 2.dp else 1.4.dp, if (selected) scheme.primary else scheme.outlineVariant, shape)
            .selectable(selected = selected, enabled = enabled, onClick = onClick, role = Role.RadioButton),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun DashedAddButton(label: String, onClick: () -> Unit) {
    val dash = MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(ShoplyShapes.card)
            .drawBehind {
                drawRoundRect(
                    color = dash,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                )
            }
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(ShoplyAppIcons.Plus, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        Text(label, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TrailingText(text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Text(text = text, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = color)
}

@Composable
private fun SectionTitle(text: String, first: Boolean = false) {
    Text(
        text = text,
        fontSize = 15.5.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = if (first) 4.dp else 24.dp, bottom = 12.dp),
    )
}
