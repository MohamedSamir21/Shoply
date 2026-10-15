package com.shopping.shoply.authentication.presentation

import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.utility.UiText

/**
 * Accessibility labels for auth controls that have no visible text of their own, kept together so
 * the same wording reaches every call site.
 */
internal object Description {
    val BACK_PRESS = UiText.StringResource(id = R.string.back)
    val LINEAR_PROGRESS = UiText.StringResource(id = R.string.linear_progress)
}