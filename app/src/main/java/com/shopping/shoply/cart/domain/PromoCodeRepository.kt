package com.shopping.shoply.cart.domain

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

/** Looks up promo codes. */
interface PromoCodeRepository {

    /** The discount [code] gives, e.g. 10 for 10% off. Null when there's no such active code. */
    suspend fun percentOff(code: String): Result<Int?, DataError>
}
