package com.shopping.shoply.authentication.domain.model

// Domain-level representation of password strength. Deliberately framework- and
// library-agnostic: zxcvbn's score (0..4) is translated into this by the data layer,
// so domain and presentation never depend on the library itself. Mirrors how
// ValidationError keeps user-facing concerns out of the domain.
enum class PasswordStrength {
    NONE,    // empty field — render nothing
    WEAK,    // zxcvbn score 0..1
    FAIR,    // zxcvbn score 2
    GOOD,    // zxcvbn score 3
    STRONG   // zxcvbn score 4
}