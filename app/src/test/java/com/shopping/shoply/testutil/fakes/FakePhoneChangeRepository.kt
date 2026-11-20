package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.changephone.domain.PhoneChangeRepository
import com.shopping.shoply.changephone.domain.PhoneCodeEvent
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Script Firebase's answers with [passwordResult], [sendEvent] and [verifyResult]; [sentTo] records each number texted. */
class FakePhoneChangeRepository(
    var phone: String? = null,
    var hasPassword: Boolean = true
) : PhoneChangeRepository {
    var passwordResult: Result<Unit, DataError> = Result.Success(Unit)
    var sendEvent: PhoneCodeEvent = PhoneCodeEvent.CodeSent
    var verifyResult: Result<Unit, DataError> = Result.Success(Unit)
    val passwordsChecked = mutableListOf<String>()
    val sentTo = mutableListOf<String>()

    override fun currentPhone() = phone
    override fun needsPassword() = hasPassword

    override suspend fun confirmPassword(password: String): Result<Unit, DataError> {
        passwordsChecked += password
        return passwordResult
    }

    override fun sendCode(e164: String): Flow<PhoneCodeEvent> {
        sentTo += e164
        return flowOf(sendEvent)
    }

    override suspend fun verifyCode(code: String) = verifyResult
}
