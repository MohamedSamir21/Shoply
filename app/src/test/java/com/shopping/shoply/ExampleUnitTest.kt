package com.shopping.shoply

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * Smoke test for the testing toolchain itself.
 *
 * If this runs and passes after a Gradle sync, three things are proven wired up:
 *  - the **android-junit5** plugin is running **JUnit 5 (Jupiter)** tests (`org.junit.jupiter.api.Test`),
 *  - **Google Truth** is on the test classpath (`assertThat(...)`),
 *  - the host (JVM) test source set compiles and executes.
 *
 * It deliberately asserts nothing about the app — its only job is to fail loudly if the test
 * stack is misconfigured.
 */
class ExampleUnitTest {
    @Test
    fun `toolchain is wired up`() {
        assertThat(2 + 2).isEqualTo(4)
    }
}
