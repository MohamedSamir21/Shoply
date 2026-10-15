package com.shopping.shoply.authentication.presentation.utility

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A coroutine-based replacement for [android.os.CountDownTimer]: ticks every [intervalMillis]
 * until [durationMillis] elapses, then fires [onFinish]. Runs on [scope], so it needs no manual
 * cancellation on ViewModel teardown — cancelling `viewModelScope` cancels this along with it.
 * Emits raw milliseconds remaining; formatting for display is the caller's concern.
 */
class CountdownTicker(
    private val scope: CoroutineScope,
    private val durationMillis: Long,
    private val intervalMillis: Long,
    private val onTick: (Long) -> Unit,
    private val onFinish: () -> Unit
) {
    private var job: Job? = null

    fun start() {
        job?.cancel()
        job = scope.launch {
            var remaining = durationMillis
            while (remaining > 0) {
                delay(intervalMillis)
                remaining -= intervalMillis
                if (remaining > 0) onTick(remaining) else onFinish()
            }
        }
    }
}
