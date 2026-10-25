package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.cart.domain.PromoCodeRepository
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

/**
 * Answers from [codes] (code → percent off); an unknown code gives null. Fails with [error] when
 * set. [gate] keeps a lookup pending; [lookups] records the codes asked for.
 */
class FakePromoCodeRepository(
    var codes: Map<String, Int> = emptyMap(),
    var error: DataError? = null,
) : PromoCodeRepository {
    val gate = Gate()
    val lookups = mutableListOf<String>()

    override suspend fun percentOff(code: String): Result<Int?, DataError> {
        lookups += code
        gate.pass()
        error?.let { return Result.Error(it) }
        return Result.Success(codes[code])
    }
}
