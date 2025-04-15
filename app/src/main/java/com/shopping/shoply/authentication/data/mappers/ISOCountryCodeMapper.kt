package com.shopping.shoply.authentication.data.mappers

import com.shopping.shoply.authentication.domain.model.ISOCountryCode

fun String.toDomainISOCountryCode(): ISOCountryCode = ISOCountryCode(this)

fun ISOCountryCode.toList(): List<ISOCountryCode> = listOf(this)
