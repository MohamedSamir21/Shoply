package com.shopping.shoply.authentication.data.datasource.country_info

import android.content.Context
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// Context is injected here in the data layer (where Android deps belong) instead of
// being passed down through the domain — so domain no longer needs a Context handle.
class TelephonyManagerDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    fun getISOCountryCode(): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.networkCountryIso
    }
}