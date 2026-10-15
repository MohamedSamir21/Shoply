package com.shopping.shoply.authentication.data.mapper

import com.shopping.shoply.authentication.domain.model.ISOCountryCode

/**
 * Wraps a raw ISO 3166 region code (e.g. `"EG"`) in the domain [ISOCountryCode] value class, so the
 * rest of the app passes a typed region rather than a bare [String].
 */
fun String.toDomainISOCountryCode(): ISOCountryCode = ISOCountryCode(this)

/**
 * Wraps this single [ISOCountryCode] in a one-element list.
 *
 * Lets a caller that resolves the device's single detected region present it as a
 * `List<ISOCountryCode>`, uniform with the multi-region path (see `CountryInfoRepositoryImpl`).
 */
fun ISOCountryCode.toList(): List<ISOCountryCode> = listOf(this)
