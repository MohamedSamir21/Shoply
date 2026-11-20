package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.changeemail.domain.EmailChangeRepository
import com.shopping.shoply.changeemail.domain.EmailChangeStatus
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

/**
 * Set [requestResult], [resendResult] and [status] to script Firebase's answers;
 * [requests] records each (new email, password) pair sent.
 */
class FakeEmailChangeRepository(
    var email: String? = "alex@mail.com",
    var pending: String? = null
) : EmailChangeRepository {
    var requestResult: Result<Unit, DataError> = Result.Success(Unit)
    var resendResult: Result<Unit, DataError> = Result.Success(Unit)
    var status: Result<EmailChangeStatus, DataError> = Result.Success(EmailChangeStatus.WAITING)
    val requests = mutableListOf<Pair<String, String>>()
    var cancelled = false

    override fun currentEmail() = email
    override suspend fun pendingEmail() = pending

    override suspend fun requestEmailChange(newEmail: String, password: String): Result<Unit, DataError> {
        requests += newEmail to password
        return requestResult
    }

    override suspend fun resendEmailChangeLink(newEmail: String) = resendResult

    override fun cancelEmailChange() {
        cancelled = true
        pending = null
    }

    override suspend fun checkEmailChange(newEmail: String) = status
}
