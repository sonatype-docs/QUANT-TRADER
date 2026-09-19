package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class QuantKitColors(
    val canvas: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val container: Color,
    val containerSubtle: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color = SunsetOrange,
    val primaryHover: Color = SunsetOrangeDark,
    val pnlPositive: Color = PnlPositive,
    val pnlPositiveBg: Color = PnlPositiveBg,
    val pnlCyan: Color = PnlPositiveCyan,
    val pnlCyanBg: Color = PnlCyanBg,
    val pnlNegative: Color = PnlNegative,
    val pnlNegativeBg: Color = PnlNegativeBg,
    val scanning: Color = StatusScanning,
    val scanningBg: Color = StatusScanningBg,
    val mlPurple: Color = MlEnginePurple,
    val isDark: Boolean = true
)

val LocalQuantKitColors = staticCompositionLocalOf {
    QuantKitColors(
        canvas = DarkCanvas,
        surface = DarkSurface,
        surfaceAlt = DarkSurfaceAlt,
        container = DarkContainer,
        containerSubtle = DarkContainerSubtle,
        border = DarkBorder,
        borderSubtle = DarkBorderSubtle,
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary,
        textMuted = DarkTextMuted,
        isDark = true
    )
}

val DarkQuantKitColors = QuantKitColors(
    canvas = DarkCanvas,
    surface = DarkSurface,
    surfaceAlt = DarkSurfaceAlt,
    container = DarkContainer,
    containerSubtle = DarkContainerSubtle,
    border = DarkBorder,
    borderSubtle = DarkBorderSubtle,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    isDark = true
)

val LightQuantKitColors = QuantKitColors(
    canvas = LightCanvas,
    surface = LightSurface,
    surfaceAlt = LightSurfaceAlt,
    container = LightContainer,
    containerSubtle = LightContainerSubtle,
    border = LightBorder,
    borderSubtle = LightBorderSubtle,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    isDark = false
)

private val DarkColorScheme = darkColorScheme(
    primary = SunsetOrange,
    onPrimary = Color.White,
    primaryContainer = SunsetOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = PnlPositiveCyan,
    onSecondary = Color.Black,
    tertiary = MlEnginePurple,
    background = DarkCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkContainer,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = SunsetOrange,
    onPrimary = Color.White,
    primaryContainer = SunsetOrangeLight,
    onPrimaryContainer = Color.Black,
    secondary = PnlPositiveCyan,
    onSecondary = Color.White,
    tertiary = MlEnginePurple,
    background = LightCanvas,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightContainer,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val quantKitColors = if (darkTheme) DarkQuantKitColors else LightQuantKitColors
    val materialColors = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Keep status bar strictly black at all times
                window.statusBarColor = android.graphics.Color.BLACK
                val insetsController = WindowCompat.getInsetsController(window, view)
                // false means light text/icons on dark/black status bar
                insetsController.isAppearanceLightStatusBars = false
            }
        }
    }

    CompositionLocalProvider(LocalQuantKitColors provides quantKitColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography,
            content = content
        )
    }
}
