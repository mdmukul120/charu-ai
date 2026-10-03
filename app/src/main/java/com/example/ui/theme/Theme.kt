package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CharuDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF001F25),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = Color(0xFFB8F5FF),
    secondary = CyberPurple,
    onSecondary = Color(0xFF1F0059),
    secondaryContainer = Color(0xFF381E72),
    onSecondaryContainer = Color(0xFFEADDFF),
    tertiary = CyberGold,
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5B4300),
    onTertiaryContainer = Color(0xFFFFE088),
    background = CyberDark,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceCard,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberSurfaceBorder,
    error = CyberRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Charu OS is an advanced AI operating system with a signature futuristic dark aesthetic
    MaterialTheme(
        colorScheme = CharuDarkColorScheme,
        typography = Typography,
        content = content
    )
}
