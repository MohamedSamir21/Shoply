package com.shopping.shoply.authentication.data.validator

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.shopping.shoply.authentication.domain.interfaces.validator.PhoneValidator
import javax.inject.Inject

/**
 * Default [PhoneValidator], implementing the domain port in the data layer.
 *
 * Delegates to Google's libphonenumber ([PhoneNumberUtil]) — the same instance already used for
 * country metadata elsewhere — rather than a regular expression. libphonenumber knows each region's
 * real numbering plan, so it rejects numbers that are the wrong length or shape for their country,
 * which a generic pattern cannot.
 *
 * The input is expected in **E.164** format: a leading `+`, the country calling code, then the
 * national number (e.g. `+201001234567`). That fully-qualified form identifies the region on its own,
 * so no separate region hint is needed. Callers assemble E.164 from the selected country's calling
 * code and the entered national number before validating.
 *
 * libphonenumber is a pure JVM library with no Android dependency, so this class stays unit-testable
 * on the host with no Android runtime.
 *
 * @see PhoneValidator the domain port this fulfils
 */
class LibphonenumberPhoneValidator @Inject constructor(
    private val phoneNumberUtil: PhoneNumberUtil,
) : PhoneValidator {

    /**
     * Returns whether [phoneNumber] is a valid number for its country.
     *
     * @param phoneNumber the number to validate, in E.164 format (e.g. `+201001234567`).
     * @return `true` when libphonenumber parses [phoneNumber] and considers it valid for the resolved
     *   region; `false` if it is malformed, unparseable, or not a valid number for that region.
     */
    override fun isValid(phoneNumber: String): Boolean =
        try {
            // Null region: the leading '+<calling code>' in an E.164 number identifies the region.
            val parsed = phoneNumberUtil.parse(phoneNumber, null)
            phoneNumberUtil.isValidNumber(parsed)
        } catch (_: NumberParseException) {
            // Unparseable input (missing '+', stray characters, empty) is simply not valid.
            false
        }
}
