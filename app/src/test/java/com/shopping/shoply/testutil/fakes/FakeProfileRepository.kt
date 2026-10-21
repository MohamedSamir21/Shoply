package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import kotlinx.coroutines.flow.MutableStateFlow

/** Set [displayName] / [primaryAddress] directly to simulate live changes. */
class FakeProfileRepository : ProfileRepository {
    val displayName = MutableStateFlow("")
    val primaryAddress = MutableStateFlow<DeliveryAddress?>(null)

    override fun observeDisplayName() = displayName
    override fun observePrimaryAddress() = primaryAddress
}
