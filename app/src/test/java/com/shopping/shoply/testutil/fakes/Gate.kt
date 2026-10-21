package com.shopping.shoply.testutil.fakes

import kotlinx.coroutines.CompletableDeferred

/**
 * Pauses a fake's suspend call so a test can check what happens while it's still running.
 *
 * Open by default. [hold] closes it: calls wait inside [pass] until [release].
 */
class Gate {
    private var latch: CompletableDeferred<Unit>? = null

    fun hold() {
        latch = CompletableDeferred()
    }

    fun release() {
        latch?.complete(Unit)
        latch = null
    }

    suspend fun pass() {
        latch?.await()
    }
}
