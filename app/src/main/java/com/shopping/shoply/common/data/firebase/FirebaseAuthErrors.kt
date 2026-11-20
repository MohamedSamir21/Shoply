package com.shopping.shoply.common.data.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.shopping.shoply.common.domain.DataError

/** Maps a Firebase failure from an account change (email or phone) to a [DataError]. */
fun Exception.toAccountChangeError(): DataError = when (this) {
    is FirebaseNetworkException -> DataError.Network
    is FirebaseTooManyRequestsException -> DataError.Authentication.TOO_MANY_REQUESTS
    is FirebaseAuthException -> accountChangeErrorFor(errorCode)
    else -> DataError.Unknown
}

/** Firebase Auth error codes to [DataError]s. Newer SDKs report a wrong password as INVALID_CREDENTIAL. */
fun accountChangeErrorFor(errorCode: String?): DataError = when (errorCode) {
    "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> DataError.Authentication.WRONG_PASSWORD
    "ERROR_EMAIL_ALREADY_IN_USE" -> DataError.Authentication.EMAIL_ALREADY_IN_USE
    "ERROR_INVALID_EMAIL" -> DataError.Authentication.INVALID_EMAIL
    "ERROR_REQUIRES_RECENT_LOGIN" -> DataError.Authentication.REQUIRES_RECENT_LOGIN
    "ERROR_TOO_MANY_REQUESTS" -> DataError.Authentication.TOO_MANY_REQUESTS
    "ERROR_CREDENTIAL_ALREADY_IN_USE" -> DataError.Authentication.PHONE_NUMBER_ALREADY_IN_USE
    "ERROR_INVALID_PHONE_NUMBER" -> DataError.Authentication.INVALID_PHONE_NUMBER
    "ERROR_INVALID_VERIFICATION_CODE" -> DataError.Authentication.INVALID_CODE
    "ERROR_SESSION_EXPIRED" -> DataError.Authentication.CODE_EXPIRED
    else -> DataError.Unknown
}
