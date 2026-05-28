package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val activeColorScheme = if (ThemeManager.currentColors.isLight) {
        lightColorScheme(
            primary = PrimaryBlue,
            onPrimary = Color(0xFFFFFFFF),
            secondary = PrimaryBlueLight,
            onSecondary = Color(0xFF1D0052),
            background = DarkBackground,
            onBackground = TextPrimary,
            surface = DarkSurface,
            onSurface = TextPrimary,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = TextSecondary,
            error = ColorExpense
        )
    } else {
        darkColorScheme(
            primary = PrimaryBlue,
            onPrimary = Color(0xFF381E72),
            secondary = PrimaryBlueLight,
            onSecondary = Color(0xFF1D0052),
            background = DarkBackground,
            onBackground = TextPrimary,
            surface = DarkSurface,
            onSurface = TextPrimary,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = TextSecondary,
            error = ColorExpense
        )
    }

    MaterialTheme(
        colorScheme = activeColorScheme,
        typography = Typography,
        content = content
    )
}
