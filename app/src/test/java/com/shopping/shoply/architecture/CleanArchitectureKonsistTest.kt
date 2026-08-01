package com.shopping.shoply.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/**
 * Architecture guardrails for the clean-architecture dependency rule.
 *
 * Single-module app: nothing in the compiler stops a `domain` file from importing
 * `data`/`presentation`/Android, so the layering was "convention only". These tests
 * turn that convention into a build failure — run as ordinary unit tests via
 * `./gradlew test`. Konsist reads source code (no bytecode/Android SDK needed).
 *
 * The rule being enforced:  presentation -> domain <- data
 *   - domain depends on nothing outward (and stays framework-free)
 *   - data may know domain, never presentation
 *   - presentation may know domain, never data
 */
class CleanArchitectureKonsistTest {

    private companion object {
        const val BASE = "com.shopping.shoply"
    }

    // Filter files by a path segment (e.g. "domain") instead of Konsist's package DSL,
    // so the rules don't depend on a specific Konsist API surface. Normalize separators
    // so it works regardless of OS (Windows '\' vs '/').
    private fun layerFiles(segment: String) =
        Konsist.scopeFromProject()
            .files
            .filter { it.path.replace('\\', '/').contains("/$segment/") }

    @Test
    fun `domain does not depend on data, presentation, or the Android framework`() {
        layerFiles("domain")
            .assertFalse { file ->
                file.imports.any { import ->
                    val ownProjectLayerLeak = import.name.startsWith(BASE) &&
                        (import.name.contains(".data.") || import.name.contains(".presentation."))
                    val frameworkLeak = import.name.startsWith("android.") ||
                        import.name.startsWith("androidx.")
                    ownProjectLayerLeak || frameworkLeak
                }
            }
    }

    @Test
    fun `data does not depend on presentation`() {
        layerFiles("data")
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith(BASE) && import.name.contains(".presentation.")
                }
            }
    }

    @Test
    fun `presentation does not depend on data directly`() {
        layerFiles("presentation")
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith(BASE) && import.name.contains(".data.")
                }
            }
    }
}