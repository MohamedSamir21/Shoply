package com.shopping.shoply.addresses.domain.rules

import com.shopping.shoply.profile.domain.model.DeliveryAddress

enum class AddressLabel { HOME, WORK, OTHER }

/** The form's required fields; apartment is optional. */
enum class AddressField { NAME, PHONE, STREET, CITY, ZIP }

/** What the Add / Edit Address form holds. Saved as [DeliveryAddress.line1] / [DeliveryAddress.line2]. */
data class AddressForm(
    val label: AddressLabel = AddressLabel.HOME,
    val recipientName: String = "",
    val phone: String = "",
    val street: String = "",
    val apartment: String = "",
    val city: String = "",
    val zip: String = "",
    val primary: Boolean = false,
)

private const val MIN_PHONE_DIGITS = 7

/** The required fields that are blank, plus [AddressField.PHONE] when it has fewer than 7 digits. */
fun invalidFields(form: AddressForm): Set<AddressField> = buildSet {
    if (form.recipientName.isBlank()) add(AddressField.NAME)
    if (form.phone.count { it.isDigit() } < MIN_PHONE_DIGITS) add(AddressField.PHONE)
    if (form.street.isBlank()) add(AddressField.STREET)
    if (form.city.isBlank()) add(AddressField.CITY)
    if (form.zip.isBlank()) add(AddressField.ZIP)
}

/**
 * The address to save: line1 = "street, apartment", line2 = "city zip". The first address is
 * always the default, whatever the switch says.
 */
fun addressFrom(form: AddressForm, id: String, isFirst: Boolean) = DeliveryAddress(
    label = form.label.displayName,
    line1 = listOf(form.street.trim(), form.apartment.trim()).filter { it.isNotEmpty() }.joinToString(", "),
    id = id,
    recipientName = form.recipientName.trim(),
    line2 = "${form.city.trim()} ${form.zip.trim()}",
    phone = form.phone.trim(),
    primary = form.primary || isFirst,
)

/** The form for editing [address]: the reverse of [addressFrom] (line2's last word is the ZIP). */
fun formOf(address: DeliveryAddress) = AddressForm(
    label = AddressLabel.entries.firstOrNull { it.displayName.equals(address.label, ignoreCase = true) } ?: AddressLabel.OTHER,
    recipientName = address.recipientName,
    phone = address.phone,
    street = address.line1.substringBefore(", "),
    apartment = address.line1.substringAfter(", ", ""),
    city = address.line2.substringBeforeLast(" ", address.line2),
    zip = address.line2.substringAfterLast(" ", ""),
    primary = address.primary,
)

/** Other addresses that must stop being the default when [saved] becomes it. */
fun idsToDemote(addresses: List<DeliveryAddress>, saved: DeliveryAddress): List<String> =
    if (!saved.primary) emptyList() else addresses.filter { it.primary && it.id != saved.id }.map { it.id }

/** The address that becomes the default when [deletedId] (the default) is deleted; null otherwise. */
fun idToPromote(addresses: List<DeliveryAddress>, deletedId: String): String? {
    val deleted = addresses.firstOrNull { it.id == deletedId } ?: return null
    return if (deleted.primary) addresses.firstOrNull { it.id != deletedId }?.id else null
}

val AddressLabel.displayName: String
    get() = when (this) {
        AddressLabel.HOME -> "Home"
        AddressLabel.WORK -> "Work"
        AddressLabel.OTHER -> "Other"
    }
