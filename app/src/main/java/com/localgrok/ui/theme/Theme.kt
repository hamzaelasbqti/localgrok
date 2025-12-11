package com.localgrok.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

// ═══════════════════════════════════════════════════════════════════════════
// LOCALGROK THEME - SUPPORTS SPACE AND DARK
// ═══════════════════════════════════════════════════════════════════════════

private fun createColorScheme(colors: LocalGrokColors) = darkColorScheme(
    // Primary - White/Grey based
    primary = colors.textPrimary,
    onPrimary = colors.background,
    primaryContainer = colors.darkGrey,
    onPrimaryContainer = colors.textPrimary,

    // Secondary - Grey tones
    secondary = colors.mediumGrey,
    onSecondary = colors.textPrimary,
    secondaryContainer = colors.darkGrey,
    onSecondaryContainer = colors.textPrimary,

    // Tertiary
    tertiary = colors.textSecondary,
    onTertiary = colors.background,
    tertiaryContainer = colors.darkGrey,
    onTertiaryContainer = colors.textPrimary,

    // Background
    background = colors.background,
    onBackground = colors.textPrimary,

    // Surface
    surface = colors.background,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.darkGrey,
    onSurfaceVariant = colors.textSecondary,
    surfaceTint = colors.textPrimary,

    // Inverse
    inverseSurface = colors.textPrimary,
    inverseOnSurface = colors.background,
    inversePrimary = colors.darkGrey,

    // Error
    error = colors.error,
    onError = colors.textPrimary,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    // Outline
    outline = colors.borderGrey,
    outlineVariant = colors.mediumGrey,

    // Scrim
    scrim = colors.background
)

@Composable
fun LocalGrokTheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val colors = when (appTheme) {
        AppTheme.SPACE -> SpaceColors
        AppTheme.DARK -> DarkColors
    }

    val colorScheme = createColorScheme(colors)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as ComponentActivity
            // Use the modern enableEdgeToEdge API with theme-aware system bars
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(colors.background.toArgb()),
                navigationBarStyle = SystemBarStyle.dark(colors.background.toArgb())
            )
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalAppTheme provides appTheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LocalGrokTypography,
            content = content
        )
    }
}
