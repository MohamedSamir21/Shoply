package com.shopping.shoply.common.presentation.util

/** Cents to display text: 8900 → "$89.00". Always US format, whatever the phone's locale. */
fun formatPrice(cents: Long): String = "$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
