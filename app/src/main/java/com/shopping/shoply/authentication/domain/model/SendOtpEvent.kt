package com.shopping.shoply.authentication.domain.model

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

// Models the two outcomes of an OTP request as a stream of domain events
// instead of framework-style callbacks, so the contract stays declarative.
sealed interface SendOtpEvent {
    // The code request was started/sent — the UI can move to the verify step.
    data object CodeSent : SendOtpEvent

    // Android auto-retrieved and verified the SMS without manual entry.
    data class AutoVerified(val result: Result<Unit, DataError>) : SendOtpEvent
}