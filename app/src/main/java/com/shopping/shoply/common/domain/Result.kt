package com.shopping.shoply.common.domain

typealias RootError = Error

sealed interface Result<out D, out E : RootError> {
    data class Success<out D, out E : RootError>(val data: D) : Result<D, E>
    data class Error<out D, out E : RootError>(val error: E) : Result<D, E>
}

// Returns the error on the Error branch, or null on Success. Lets callers treat a
// Result as a nullable-error flag where that reads cleaner (e.g. form validation).
fun <D, E : RootError> Result<D, E>.errorOrNull(): E? =
    (this as? Result.Error)?.error