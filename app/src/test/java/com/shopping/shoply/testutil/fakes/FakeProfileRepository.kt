package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.Account
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Set [displayName] / [primaryAddress] / [account] directly to simulate live changes.
 * [silentAccount] makes [observeAccount] never emit, like a listener stuck on a permission error.
 */
class FakeProfileRepository : ProfileRepository {
    val displayName = MutableStateFlow("")
    val primaryAddress = MutableStateFlow<DeliveryAddress?>(null)
    val account = MutableStateFlow(Account(name = "", email = "", phone = null))
    var silentAccount = false

    override fun observeDisplayName() = displayName
    override fun observePrimaryAddress() = primaryAddress
    override fun observeAccount(): Flow<Account> =
        if (silentAccount) flow { awaitCancellation() } else account
}
