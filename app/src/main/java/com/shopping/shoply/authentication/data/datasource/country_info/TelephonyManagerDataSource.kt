package com.shopping.shoply.authentication.data.datasource.country_info

import android.content.Context
import android.telephony.TelephonyManager

class TelephonyManagerDataSource {
    fun getISOCountryCode(context: Context): String? {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val isoCountryCode = telephonyManager.networkCountryIso
        return isoCountryCode
    }
}