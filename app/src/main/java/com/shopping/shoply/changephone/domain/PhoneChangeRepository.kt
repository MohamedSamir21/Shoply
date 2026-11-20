package com.shopping.shoply.changephone.domain

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * Adds or changes the signed-in user's phone number, verified by an SMS code (spec FR-040 to FR-046).
 * The number is linked to the Firebase Auth account and saved to `users/{uid}` in E.164 form.
 */
interface PhoneChangeRepository {

    /** The linked phone number in E.164 form, or null when none is linked. */
    fun currentPhone(): String?

    /** True for accounts with a password: they confirm it before a code is sent (FR-045). */
    fun needsPassword(): Boolean

    /** Re-checks the password. Fails with [DataError.Authentication.WRONG_PASSWORD]. */
    suspend fun confirmPassword(password: String): Result<Unit, DataError>

    /**
     * Texts a code to [e164]. Emits [PhoneCodeEvent.CodeSent] once the code is on its way, or
     * [PhoneCodeEvent.Finished] when Android verified the number by itself or the request failed.
     */
    fun sendCode(e164: String): Flow<PhoneCodeEvent>

    /** Checks the [code] the user typed and, when right, links the number to the account. */
    suspend fun verifyCode(code: String): Result<Unit, DataError>
}

sealed interface PhoneCodeEvent {
    data object CodeSent : PhoneCodeEvent

    /** No code to type: the number was verified automatically ([Result.Success]) or sending failed. */
    data class Finished(val result: Result<Unit, DataError>) : PhoneCodeEvent
}
