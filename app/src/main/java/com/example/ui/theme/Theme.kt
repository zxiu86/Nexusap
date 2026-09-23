package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private data class AccentColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color
)

fun getAppColorScheme(
    isDark: Boolean,
    backgroundStyle: Int, // 0: Default, 1: AMOLED Pure Black, 2: Pure White
    accentColor: Int // 0..8: Solid, 10..18: Multi-Color Gradients
): ColorScheme {
    val preset = ThemePalettes.getPresetById(accentColor)
    val isWhiteTheme = preset.id == 8 || preset.id == 18

    val (primary, onPrimary, primaryContainer, onPrimaryContainer, secondary, secondaryContainer) = if (isWhiteTheme) {
        if (isDark) {
            AccentColors(
                primary = NexusWhitePrimary,
                onPrimary = Color(0xFF0F172A),
                primaryContainer = Color.White.copy(alpha = 0.18f),
                onPrimaryContainer = Color.White,
                secondary = NexusWhiteDark,
                secondaryContainer = Color.White.copy(alpha = 0.10f)
            )
        } else {
            AccentColors(
                primary = Color(0xFF0F172A),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFF1F5F9),
                onPrimaryContainer = Color(0xFF0F172A),
                secondary = Color(0xFF334155),
                secondaryContainer = Color(0xFFE2E8F0)
            )
        }
    } else {
        AccentColors(
            primary = preset.primaryColor,
            onPrimary = preset.onPrimary,
            primaryContainer = preset.primaryColor.copy(alpha = 0.25f),
            onPrimaryContainer = if (isDark) TextPrimary else TextPrimaryLight,
            secondary = preset.secondaryColor,
            secondaryContainer = preset.secondaryColor.copy(alpha = 0.2f)
        )
    }

    return if (isDark) {
        val (bg, surf, surfVar, surfElev) = when (backgroundStyle) {
            1 -> listOf(BackgroundAmoled, SurfaceAmoled, SurfaceVariantAmoled, SurfaceElevatedAmoled)
            else -> listOf(BackgroundDark, SurfaceDark, SurfaceVariantDark, SurfaceElevated)
        }

        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = TextPrimary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = TextPrimary,
            tertiary = if (isWhiteTheme) Color.White else NexusGold,
            onTertiary = bg,
            background = bg,
            onBackground = TextPrimary,
            surface = surf,
            onSurface = TextPrimary,
            surfaceVariant = surfVar,
            onSurfaceVariant = TextSecondary,
            outline = surfElev
        )
    } else {
        val bg = if (backgroundStyle == 2) Color(0xFFFAFAFC) else BackgroundLight
        lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = TextPrimaryLight,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = TextPrimaryLight,
            tertiary = if (isWhiteTheme) Color(0xFF475569) else NexusOrange,
            onTertiary = Color.White,
            background = bg,
            onBackground = TextPrimaryLight,
            surface = SurfaceLight,
            onSurface = TextPrimaryLight,
            surfaceVariant = SurfaceVariantLight,
            onSurfaceVariant = TextSecondaryLight,
            outline = SurfaceElevatedLight
        )
    }
}

@Composable
fun NexusTheme(
    themeMode: Int = 0, // 0: System, 1: Dark, 2: Light
    backgroundStyle: Int = 0, // 0: Default, 1: AMOLED Black, 2: Pure White
    accentColor: Int = 0, // 0..7: Solid, 10..15: Multi-Color Gradients
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        1 -> true
        2 -> false
        else -> darkTheme
    }

    val colorScheme = getAppColorScheme(
        isDark = isDark,
        backgroundStyle = backgroundStyle,
        accentColor = accentColor
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

