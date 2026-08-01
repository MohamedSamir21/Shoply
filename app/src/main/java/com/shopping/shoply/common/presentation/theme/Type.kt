package com.shopping.shoply.common.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val jakarta = GoogleFont("Plus Jakarta Sans")

// One family across the whole product (Plus Jakarta Sans).
val plusJakartaSans = FontFamily(
    Font(googleFont = jakarta, fontProvider = provider, weight = FontWeight.Medium),     // 500 — body default
    Font(googleFont = jakarta, fontProvider = provider, weight = FontWeight.SemiBold),   // 600
    Font(googleFont = jakarta, fontProvider = provider, weight = FontWeight.Bold),       // 700
    Font(googleFont = jakarta, fontProvider = provider, weight = FontWeight.ExtraBold),  // 800
)

// Convenience aliases (kept for any existing references).
val bodyFontFamily = plusJakartaSans
val displayFontFamily = plusJakartaSans

private val tight = (-0.01).em

/* ============================================================
   Type scale from tokens/typography.css. Headings are tight &
   bold (700); body sits at a slightly heavier 500 for crispness.
   ============================================================ */
val AppTypography = Typography(
    // screen titles: "Welcome Back" (27 / 700)
    displaySmall = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold,
        fontSize = 27.sp, lineHeight = 32.sp, letterSpacing = tight
    ),
    headlineMedium = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold,
        fontSize = 27.sp, lineHeight = 32.sp, letterSpacing = tight
    ),
    // onboarding headline (25 / 700)
    titleLarge = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold,
        fontSize = 25.sp, lineHeight = 30.sp, letterSpacing = tight
    ),
    // app-bar / section title (19 / 700)
    headlineSmall = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold,
        fontSize = 19.sp, lineHeight = 24.sp, letterSpacing = tight
    ),
    titleMedium = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.Bold,
        fontSize = 19.sp, lineHeight = 24.sp, letterSpacing = tight
    ),
    // emphasised control label (16 / 600)
    titleSmall = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 22.sp
    ),
    // paragraphs / field text (15.5 / 500)
    bodyLarge = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium,
        fontSize = 15.5.sp, lineHeight = 24.sp
    ),
    // dense supporting copy (14.5 / 500)
    bodyMedium = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium,
        fontSize = 14.5.sp, lineHeight = 22.sp
    ),
    // links, helper, button-adjacent (13.5 / 600)
    labelLarge = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold,
        fontSize = 13.5.sp, lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, lineHeight = 18.sp
    ),
    // meta, kicker, field labels (12.5)
    bodySmall = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp, lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = plusJakartaSans, fontWeight = FontWeight.SemiBold,
        fontSize = 12.5.sp, lineHeight = 16.sp
    ),
)
