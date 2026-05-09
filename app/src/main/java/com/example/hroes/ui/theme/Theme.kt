package com.example.hroes.ui.theme


import com.example.hroes.BuildConfig
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = Green100,
    onPrimaryContainer = GreenDark,
    secondary = TealAccent,
    onSecondary = TextOnPrimary,
    secondaryContainer = TealLight,
    onSecondaryContainer = TealDark,
    tertiary = AccentBlue,
    onTertiary = TextOnPrimary,
    tertiaryContainer = AccentBlueLight,
    onTertiaryContainer = AccentBlueDark,
    background = SurfaceLight,
    onBackground = TextPrimary,
    surface = CardWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceGreenTint,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = AccentRed,
    onError = TextOnPrimary,
    errorContainer = AccentRedLight,
    onErrorContainer = AccentRedDark,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkGreenPrimary,
    onPrimary = DarkSurface,
    primaryContainer = GreenDark,
    onPrimaryContainer = GreenLight,
    secondary = DarkTealAccent,
    onSecondary = DarkSurface,
    secondaryContainer = TealDark,
    onSecondaryContainer = TealLight,
    tertiary = AccentBlue,
    onTertiary = DarkSurface,
    tertiaryContainer = AccentBlueDark,
    onTertiaryContainer = AccentBlueLight,
    background = DarkBackground,
    onBackground = TextOnDark,
    surface = DarkCard,
    onSurface = TextOnDark,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = TextTertiary,
    outline = TextTertiary,
    error = AccentRed,
    onError = TextOnPrimary,
    errorContainer = AccentRedDark,
    onErrorContainer = AccentRedLight,
)

val HRShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun BinaHrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = HRShapes,
        content = content
    )
}






















