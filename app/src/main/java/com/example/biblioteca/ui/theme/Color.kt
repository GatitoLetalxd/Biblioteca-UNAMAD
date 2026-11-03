package com.example.biblioteca.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =============================================================================
// UNAMAD PALETTE (Material 3 Adaptive - Minimalist Dark Mode)
// =============================================================================

// Light Theme Primaries & Accents
val UnamadGreenPrimary = Color(0xFF164E3F)
val UnamadGreenLight = Color(0xFF2E6B57)
val UnamadGreenDark = Color(0xFF0D382B)
val UnamadGreenContainerLight = Color(0xFFDCFCE7)
val UnamadGreenOnContainerLight = Color(0xFF064E3B)

val AmberAccent = Color(0xFFD97706)
val AmberAccentLight = Color(0xFFF59E0B)
val AmberAccentDark = Color(0xFFB45309)
val AmberContainerLight = Color(0xFFFEF3C7)
val AmberOnContainerLight = Color(0xFF78350F)

// Light Theme Neutrals & Surfaces (High-clarity paper & slate)
val ParchmentLight = Color(0xFFF8FAF9)
val SurfaceWarm = Color(0xFFFFFFFF)
val CardBackgroundLight = Color(0xFFEDF2EE)
val TextPrimaryDark = Color(0xFF0F172A)
val TextSecondaryDark = Color(0xFF475569)
val BorderSubtleLight = Color(0xFFCBD5E1)

// =============================================================================
// SLEEK MINIMALIST DARK THEME (Zero Green - Deep Obsidian & Warm Amber Accent)
// =============================================================================

val DarkPrimary = Color(0xFFFBBF24) // Warm Amber Gold
val DarkOnPrimary = Color(0xFF1C1917) // Deep Charcoal (Contrast > 11:1)
val DarkPrimaryContainer = Color(0xFF292524) // Charcoal Amber Container
val DarkOnPrimaryContainer = Color(0xFFFDE68A)

val DarkSecondary = Color(0xFF94A3B8) // Slate Neutral
val DarkOnSecondary = Color(0xFF0F172A)
val DarkSecondaryContainer = Color(0xFF1E293B)
val DarkOnSecondaryContainer = Color(0xFFE2E8F0)

val BackgroundDark = Color(0xFF0B0F17) // Pure Deep Obsidian Black / Slate
val SurfaceDark = Color(0xFF121722)    // Minimalist Dark Surface
val CardBackgroundDark = Color(0xFF1A2130) // Elevated Dark Slate Card
val TextPrimaryLight = Color(0xFFF8FAFC) // Crisp White Text (Contrast > 16:1)
val TextSecondaryLight = Color(0xFF94A3B8) // Slate 400 Text (Contrast > 7:1)
val BorderSubtleDark = Color(0xFF2B3346) // Subtle Slate Border



// Error Tokens
val ErrorLight = Color(0xFFDC2626)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF991B1B)

val ErrorDark = Color(0xFFF87171)
val OnErrorDark = Color(0xFF450A0A)
val ErrorContainerDark = Color(0xFF7F1D1D)
val OnErrorContainerDark = Color(0xFFFECACA)

// =============================================================================
// ADAPTIVE SEMANTIC STATUS COLORS (Light & Dark)
// =============================================================================

data class StatusPalette(
    val bg: Color,
    val text: Color,
    val border: Color
)

data class AdaptiveStatusColors(
    val available: StatusPalette,
    val loan: StatusPalette,
    val overdue: StatusPalette,
    val digital: StatusPalette
)

val LightStatusColors = AdaptiveStatusColors(
    available = StatusPalette(
        bg = Color(0xFFDCFCE7),
        text = Color(0xFF166534),
        border = Color(0xFF86EFAC)
    ),
    loan = StatusPalette(
        bg = Color(0xFFFEF3C7),
        text = Color(0xFF92400E),
        border = Color(0xFFFCD34D)
    ),
    overdue = StatusPalette(
        bg = Color(0xFFFEE2E2),
        text = Color(0xFF991B1B),
        border = Color(0xFFFCA5A5)
    ),
    digital = StatusPalette(
        bg = Color(0xFFE0E7FF),
        text = Color(0xFF3730A3),
        border = Color(0xFFA5B4FC)
    )
)

val DarkStatusColors = AdaptiveStatusColors(
    available = StatusPalette(
        bg = Color(0xFF1E293B),
        text = Color(0xFF38BDF8),
        border = Color(0xFF334155)
    ),
    loan = StatusPalette(
        bg = Color(0xFF2E2214),
        text = Color(0xFFFDE68A),
        border = Color(0xFF78350F)
    ),
    overdue = StatusPalette(
        bg = Color(0xFF3B1219),
        text = Color(0xFFFCA5A5),
        border = Color(0xFF991B1B)
    ),
    digital = StatusPalette(
        bg = Color(0xFF1E2238),
        text = Color(0xFFC7D2FE),
        border = Color(0xFF4338CA)
    )
)

val LocalAdaptiveStatusColors = staticCompositionLocalOf { LightStatusColors }

// Backward compatibility dynamic adaptive values
val StatusAvailableBg: Color
    @Composable get() = LocalAdaptiveStatusColors.current.available.bg

val StatusAvailableText: Color
    @Composable get() = LocalAdaptiveStatusColors.current.available.text

val StatusOutBg: Color
    @Composable get() = LocalAdaptiveStatusColors.current.overdue.bg

val StatusOutText: Color
    @Composable get() = LocalAdaptiveStatusColors.current.overdue.text

val StatusLoanBg: Color
    @Composable get() = LocalAdaptiveStatusColors.current.loan.bg

val StatusLoanText: Color
    @Composable get() = LocalAdaptiveStatusColors.current.loan.text

val StatusDigitalBg: Color
    @Composable get() = LocalAdaptiveStatusColors.current.digital.bg

val StatusDigitalText: Color
    @Composable get() = LocalAdaptiveStatusColors.current.digital.text
