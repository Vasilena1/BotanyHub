package com.example.botanyhub.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Colour Palette ────────────────────────────────────────────────────────────
val GreenPrimary    = Color(0xFF2D6A4F)
val GreenLight      = Color(0xFF52B788)
val GreenContainer  = Color(0xFFD8F3DC)
val GreenSurface    = Color(0xFFF5FBF6)
val TextPrimary     = Color(0xFF1B2E22)
val TextSecondary   = Color(0xFF4A6358)
val BgWhite         = Color(0xFFFAFEFB)
val CardWhite       = Color(0xFFFFFFFF)
val WaterBlue       = Color(0xFF6DB3CE)
val SunYellow       = Color(0xFFF4C542)
val TempOrange      = Color(0xFFE2823A)
val HumidTeal       = Color(0xFF52B788)

// ── Material3 colour scheme ───────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary             = GreenPrimary,
    onPrimary           = Color.White,
    primaryContainer    = GreenContainer,
    onPrimaryContainer  = GreenPrimary,
    secondary           = GreenLight,
    onSecondary         = Color.White,
    secondaryContainer  = Color(0xFFCCEDD8),
    onSecondaryContainer = GreenPrimary,
    background          = BgWhite,
    onBackground        = TextPrimary,
    surface             = CardWhite,
    onSurface           = TextPrimary,
    surfaceVariant      = GreenSurface,
    onSurfaceVariant    = TextSecondary,
    outline             = Color(0xFFB7D5C1),
    error               = Color(0xFFBA1A1A),
    onError             = Color.White,
)

// ── Theme composable ──────────────────────────────────────────────────────────
@Composable
fun BotanyHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColors   // light-only for now
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography   = Typography,
        content      = content
    )
}