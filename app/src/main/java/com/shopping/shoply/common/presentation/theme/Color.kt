package com.shopping.shoply.common.presentation.theme

import androidx.compose.ui.graphics.Color

/* ============================================================
   SHOPLY · COLOR TOKENS  (from Shoply Remix design system)
   Calm terracotta-orange over warm neutrals. Orange is an
   accent (buttons, icons, focus) — never a large muddy fill.
   Light is default; dark is a scoped theme.
   ============================================================ */

/* —— Light —— */
val orangeLight = Color(0xFFDE7A48)        // primary accent
val orangePressLight = Color(0xFFC96A3B)   // pressed / darker accent
val onOrangeLight = Color(0xFFFFFFFF)
val orangeSoftLight = Color(0xFFFBEADF)    // soft container behind icons
val onOrangeSoftLight = Color(0xFF7A3D17)

val bgLight = Color(0xFFFAF8F6)            // app background (warm off-white)
val surfaceLight = Color(0xFFFFFFFF)       // sheets, splash, cards
val surfaceAltLight = Color(0xFFF4F0EC)    // input fill, resting tiles
val surfaceHiLight = Color(0xFFEFEAE4)     // hover / pressed tile

val inkLight = Color(0xFF211D1A)           // primary text
val inkMuteLight = Color(0xFF766C63)       // secondary text
val inkFaintLight = Color(0xFFA79E94)      // disabled / hint

val lineLight = Color(0xFFE8E1D9)          // hairline divider / resting border
val lineStrongLight = Color(0xFFD3C9BE)    // outlined control border

val greenLight = Color(0xFF7C9E36)         // success
val greenSoftLight = Color(0xFFEEF2DC)
val redLight = Color(0xFFCF5740)           // error / destructive
val redSoftLight = Color(0xFFFBE3DC)

/* —— Dark —— */
val orangeDark = Color(0xFFE6885A)
val orangePressDark = Color(0xFFD9774C)
val onOrangeDark = Color(0xFFFFFFFF)
val orangeSoftDark = Color(0xFF2A2730)     // neutral elevated grey container
val onOrangeSoftDark = Color(0xFFE6885A)   // orange content on the grey

val bgDark = Color(0xFF121013)
val surfaceDark = Color(0xFF1A181B)
val surfaceAltDark = Color(0xFF221F23)
val surfaceHiDark = Color(0xFF2A262B)

val inkDark = Color(0xFFECE8E4)
val inkMuteDark = Color(0xFFA69E96)
val inkFaintDark = Color(0xFF6F665F)

val lineDark = Color(0xFF322E33)
val lineStrongDark = Color(0xFF46414A)

val greenDark = Color(0xFF9CC04B)
val greenSoftDark = Color(0xFF2E3A1C)
val redDark = Color(0xFFE8755F)
val redSoftDark = Color(0xFF3A201B)

/* —— Brand exception (same in both themes) —— */
val facebookBlue = Color(0xFF1877F2)
