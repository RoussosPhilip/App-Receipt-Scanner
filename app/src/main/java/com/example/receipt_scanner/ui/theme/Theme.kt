package com.example.receipt_scanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Neutral40,
    onPrimary = Color.White,
    primaryContainer = Neutral90,
    onPrimaryContainer = Neutral10,

    secondary = Neutral50,
    onSecondary = Color.White,
    secondaryContainer = Neutral80,
    onSecondaryContainer = Neutral10,

    tertiary = AccentBlue,
    onTertiary = Color.White,
    tertiaryContainer = AccentBlueLight,
    onTertiaryContainer = Neutral10,

    background = Neutral98,
    onBackground = Neutral10,

    surface = Color.White,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral50,

    error = ErrorRed,
    onError = Color.White,

    outline = Neutral80,
    outlineVariant = Neutral90
)

private val DarkColorScheme = darkColorScheme(
    primary = Neutral70,
    onPrimary = Neutral10,
    primaryContainer = Neutral30,
    onPrimaryContainer = Neutral90,

    secondary = Neutral60,
    onSecondary = Neutral10,
    secondaryContainer = Neutral30,
    onSecondaryContainer = Neutral90,

    tertiary = AccentBlueLight,
    onTertiary = Neutral10,
    tertiaryContainer = AccentBlueDark,
    onTertiaryContainer = Neutral95,

    background = Neutral5,
    onBackground = Neutral90,

    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = Neutral70,

    error = ErrorRed,
    onError = Neutral10,

    outline = Neutral40,
    outlineVariant = Neutral30
)

@Composable
fun Receipt_scannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
