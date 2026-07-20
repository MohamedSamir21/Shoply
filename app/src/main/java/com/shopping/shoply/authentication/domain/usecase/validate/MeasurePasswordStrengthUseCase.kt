package com.shopping.shoply.authentication.domain.usecase.validate

import com.shopping.shoply.authentication.domain.interfaces.validator.PasswordStrengthChecker
import com.shopping.shoply.authentication.domain.model.PasswordStrength
import javax.inject.Inject

// Application-level entry point for strength estimation. The ViewModel depends on this
// use case (not on the PasswordStrengthChecker port directly), mirroring how
// ValidateEmailUseCase wraps EmailValidator — keeping presentation one step removed from
// the port and any future estimation rules in one place.
class MeasurePasswordStrengthUseCase @Inject constructor(
    private val passwordStrengthChecker: PasswordStrengthChecker
) {
    suspend operator fun invoke(password: String): PasswordStrength =
        passwordStrengthChecker.strengthOf(password)
}