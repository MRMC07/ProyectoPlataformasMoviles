package com.example.proyectoplataformasmoviles.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = UvgGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = UvgGreenLight,
    onPrimaryContainer = UvgGreenDark,
    secondary = UvgGreenMedium,
    onSecondary = Color.White,
    secondaryContainer = UvgGreenLight,
    onSecondaryContainer = UvgGreenDark,
    tertiary = UvgGreenMedium,
    onTertiary = Color.White,
    background = UvgBackground,
    onBackground = UvgTextPrimary,
    surface = UvgSurface,
    onSurface = UvgTextPrimary,
    surfaceVariant = UvgSurfaceVariant,
    onSurfaceVariant = UvgTextSecondary,
    outline = UvgBorder,
    outlineVariant = UvgGreenBorder,
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF75DBA2),
    onPrimary = UvgDarkBackground,
    primaryContainer = Color(0xFF005230),
    onPrimaryContainer = Color(0xFF91F8BD),
    secondary = Color(0xFFFFBA38),
    onSecondary = UvgDarkBackground,
    secondaryContainer = Color(0xFF5D3F00),
    onSecondaryContainer = Color(0xFFFFDFA0),
    background = UvgDarkBackground,
    onBackground = UvgDarkTextPrimary,
    surface = UvgDarkSurface,
    onSurface = UvgDarkTextPrimary,
    surfaceVariant = UvgDarkSurfaceVariant,
    onSurfaceVariant = UvgDarkTextSecondary,
    outline = UvgDarkBorder
)

@Composable
fun ProyectoPlataformasMovilesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
