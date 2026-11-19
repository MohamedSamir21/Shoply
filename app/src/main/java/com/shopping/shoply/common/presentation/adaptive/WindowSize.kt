package com.shopping.shoply.common.presentation.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

/**
 * The app's single "is this a wide screen?" rule: anything wider than a phone in portrait
 * (landscape phones, foldables, tablets). Screens use it to switch to their two-column layouts.
 */
fun WindowSizeClass.isWide(): Boolean = windowWidthSizeClass != WindowWidthSizeClass.COMPACT

/** [isWide] for the current window. */
@Composable
fun isWideScreen(): Boolean = currentWindowAdaptiveInfo().windowSizeClass.isWide()
