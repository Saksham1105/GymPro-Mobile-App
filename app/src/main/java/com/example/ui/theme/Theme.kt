package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Black,
    secondary = SurfaceVariantDark,
    onSecondary = White,
    tertiary = AccentGreen,
    background = BackgroundDark,
    onBackground = White,
    surface = SurfaceDark,
    onSurface = White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextGray400,
    outline = BorderDark,
    error = Red,
    onError = White
)

private val LightColorScheme = DarkColorScheme // Enforce dark theme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    // Disable dynamic color for consistent branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

