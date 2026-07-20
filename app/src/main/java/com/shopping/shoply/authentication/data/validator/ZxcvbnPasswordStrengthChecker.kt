package com.shopping.shoply.authentication.data.validator

import com.nulabinc.zxcvbn.Zxcvbn
import com.shopping.shoply.authentication.domain.interfaces.validator.PasswordStrengthChecker
import com.shopping.shoply.authentication.domain.model.PasswordStrength
import com.shopping.shoply.common.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estimates how strong a password is, backed by the [zxcvbn](https://github.com/nulab/zxcvbn4j)
 * library (Nulab's JVM port of Dropbox's zxcvbn).
 *
 * This class is the **single seam** in the app where the zxcvbn library is touched. It is the
 * concrete, data-layer implementation of the domain port [PasswordStrengthChecker] — mirroring how
 * [EmailValidatorUtil] implements `EmailValidator`. By translating zxcvbn's raw `0..4` integer score
 * into the domain's [PasswordStrength] enum here, neither the domain nor the presentation layer ever
 * import or depend on zxcvbn. Swapping the estimator later (or faking it in a test) means replacing
 * only this one file.
 *
 * ### Score mapping
 * zxcvbn returns a score from 0 (trivially guessable) to 4 (very strong). It is collapsed onto
 * [PasswordStrength] as follows:
 *
 * | zxcvbn score | [PasswordStrength] |
 * |:-----------:|:-------------------|
 * | (empty input) | [PasswordStrength.NONE] |
 * | 0, 1 | [PasswordStrength.WEAK] |
 * | 2 | [PasswordStrength.FAIR] |
 * | 3 | [PasswordStrength.GOOD] |
 * | 4 | [PasswordStrength.STRONG] |
 *
 * Note that an empty password is special-cased to [PasswordStrength.NONE] *before* zxcvbn is
 * consulted, so the UI can render nothing (rather than a "weak" warning) for an untouched field.
 *
 * ### Why [Singleton]
 * Constructing a [Zxcvbn] instance loads its frequency dictionaries (common passwords, English
 * words, names, keyboard patterns) into memory. That is comparatively expensive, so the instance is
 * built once and reused across the app rather than rebuilt on every keystroke as the user types.
 *
 * @see PasswordStrengthChecker the domain port this fulfils
 * @see PasswordStrength the framework-agnostic result type
 */
@Singleton
class ZxcvbnPasswordStrengthChecker @Inject constructor(
    // CPU-bound work is confined here in the data layer, not by the caller.
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : PasswordStrengthChecker {

    private val zxcvbn = Zxcvbn()

    /**
     * Returns the [PasswordStrength] of [password].
     *
     * An empty string short-circuits to [PasswordStrength.NONE]; any other input is measured by
     * zxcvbn and the resulting score is translated by [scoreToStrength]. This function owns the two
     * impure/external concerns — the empty-input rule and the call into the zxcvbn library — while
     * the score-to-strength mapping is delegated to a pure function so it can be tested in isolation.
     *
     * @param password the raw password text to evaluate (never `null`; may be empty).
     * @return the estimated strength bucket for [password].
     */
    override suspend fun strengthOf(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.NONE
        val score = withContext(defaultDispatcher) { zxcvbn.measure(password).score }
        return scoreToStrength(score)
    }

    /**
     * Translates zxcvbn's raw `0..4` [score] into the domain's [PasswordStrength], per the table in
     * the class documentation.
     *
     * Pulled out of [strengthOf] as a **pure function** (no library call, no state) so the mapping —
     * the logic this app actually owns — can be unit-tested deterministically across every score
     * without having to hunt for input passwords that happen to produce each score. Marked
     * `internal` so it stays out of the public API yet is reachable from the same-package test.
     *
     * @param score a zxcvbn strength score; expected range `0..4`. Any value `>= 4` (and, defensively,
     *   anything not 0–3) maps to [PasswordStrength.STRONG].
     * @return the [PasswordStrength] bucket for [score].
     */
    internal fun scoreToStrength(score: Int): PasswordStrength = when (score) {
        0, 1 -> PasswordStrength.WEAK
        2 -> PasswordStrength.FAIR
        3 -> PasswordStrength.GOOD
        else -> PasswordStrength.STRONG // score == 4
    }
}