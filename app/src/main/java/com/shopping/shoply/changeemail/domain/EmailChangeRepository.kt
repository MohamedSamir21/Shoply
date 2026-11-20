package com.shopping.shoply.changeemail.domain

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

/**
 * Changes the signed-in user's sign-in email (spec FR-030 to FR-038). The new address only takes
 * effect after the user clicks the link Firebase emails to it; until then it is "pending".
 */
interface EmailChangeRepository {

    /** The current sign-in email, or null when the account has none. */
    fun currentEmail(): String?

    /** The address waiting for confirmation, or null when no change is in progress. */
    suspend fun pendingEmail(): String?

    /**
     * Confirms [password], then emails a confirmation link to [newEmail] and records it as pending.
     * Fails with [DataError.Authentication.WRONG_PASSWORD], [DataError.Authentication.EMAIL_ALREADY_IN_USE],
     * [DataError.Authentication.TOO_MANY_REQUESTS] or [DataError.Network].
     */
    suspend fun requestEmailChange(newEmail: String, password: String): Result<Unit, DataError>

    /**
     * Sends the confirmation link to [newEmail] again. Fails with
     * [DataError.Authentication.REQUIRES_RECENT_LOGIN] once the password check is too old.
     */
    suspend fun resendEmailChangeLink(newEmail: String): Result<Unit, DataError>

    /**
     * Forgets the pending address. Firebase can't revoke a link it already emailed: it stays valid
     * until it expires, so the user must be told not to click it.
     */
    fun cancelEmailChange()

    /** Whether the change to [newEmail] went through, by reloading the account from Firebase. */
    suspend fun checkEmailChange(newEmail: String): Result<EmailChangeStatus, DataError>
}

enum class EmailChangeStatus {
    /** The link hasn't been clicked yet. */
    WAITING,

    /** The account now uses the new email. */
    CONFIRMED,

    /**
     * The session ended, so the user must sign in again. Clicking the link ends it, but so does a
     * password change elsewhere or a disabled account: it does NOT prove the email changed.
     */
    SESSION_ENDED
}
