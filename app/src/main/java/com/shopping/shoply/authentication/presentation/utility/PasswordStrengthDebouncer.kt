package com.shopping.shoply.authentication.presentation.utility

import com.shopping.shoply.authentication.domain.model.PasswordStrength
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Debounces password-strength estimation so it only runs ~200ms after the user stops typing,
 * cancelling whatever measurement was in flight for the previous keystroke. Knows nothing about
 * *how* strength is measured — that's supplied by [measureStrength].
 */
class PasswordStrengthDebouncer(
    private val scope: CoroutineScope,
    private val measureStrength: suspend (String) -> PasswordStrength,
    private val onStrengthChanged: (PasswordStrength) -> Unit
) {
    private var job: Job? = null

    fun measure(password: String) {
        job?.cancel()
        if (password.isEmpty()) {
            onStrengthChanged(PasswordStrength.NONE)
            return
        }
        job = scope.launch {
            delay(200)
            onStrengthChanged(measureStrength(password))
        }
    }
}
