package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.Account
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import com.shopping.shoply.profile.domain.model.UserInfo
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Set [displayName] / [primaryAddress] / [addresses] / [account] directly to simulate live changes.
 * [silentAccount] makes [observeAccount] never emit, like a listener stuck on a permission error.
 * [updateUserInfo] records each call in [savedUserInfo] and returns [updateResult].
 */
class FakeProfileRepository : ProfileRepository {
    val displayName = MutableStateFlow("")
    val primaryAddress = MutableStateFlow<DeliveryAddress?>(null)
    val account = MutableStateFlow(Account(name = "", email = "", phone = null))
    val addresses = MutableStateFlow<List<DeliveryAddress>>(emptyList())
    var silentAccount = false
    val savedUserInfo = mutableListOf<UserInfo>()
    var updateResult: Result<Unit, DataError> = Result.Success(Unit)

    override fun observeDisplayName() = displayName
    override fun observePrimaryAddress() = primaryAddress
    override fun observeAddresses() = addresses
    override fun observeAccount(): Flow<Account> =
        if (silentAccount) flow { awaitCancellation() } else account

    override suspend fun updateUserInfo(userInfo: UserInfo): Result<Unit, DataError> {
        savedUserInfo += userInfo
        return updateResult
    }
}
