package com.shopping.shoply.profile.domain

import com.shopping.shoply.profile.domain.model.DeliveryAddress
import kotlinx.coroutines.flow.Flow

/**
 * The signed-in user's profile. Every flow emits the current value right away, then on every
 * change. They never complete and never throw: when offline they keep the last known value.
 */
interface ProfileRepository {

    /** The user's full name as entered at sign-up; empty when not set. */
    fun observeDisplayName(): Flow<String>

    /** The address marked as primary, or null when the user has none (UI shows "Add delivery address"). */
    fun observePrimaryAddress(): Flow<DeliveryAddress?>
}
