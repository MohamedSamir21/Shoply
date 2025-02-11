package com.shopping.shoply.common.domain

sealed interface DataError : Error {
    data object Unknown : DataError
    data object Network : DataError
    enum class Authentication : DataError {
        WEAK_PASSWORD,
        WRONG_PASSWORD,
        INVALID_EMAIL,
        USER_DISABLED,
        ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL,
        EMAIL_ALREADY_IN_USE,
        USER_COLLISION,
        UNKNOWN,
        NOT_VERIFIED,
        ALREADY_VERIFIED
    }
}

fun String.getAuthenticationDataError(): DataError.Authentication {
    return when (this) {
        "ERROR_WRONG_PASSWORD" -> DataError.Authentication.WRONG_PASSWORD
        "ERROR_INVALID_EMAIL" -> DataError.Authentication.INVALID_EMAIL
        "ERROR_USER_DISABLED" -> DataError.Authentication.USER_DISABLED
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> DataError.Authentication.ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL
        "ERROR_EMAIL_ALREADY_IN_USE" -> DataError.Authentication.EMAIL_ALREADY_IN_USE
        else -> DataError.Authentication.UNKNOWN
    }
}