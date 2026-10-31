package com.novaforge.launcher.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightBackground = Color(0xFFF7F6F2)
val DarkBackground = Color(0xFF121417)
val DefaultTeal = Color(0xFF2A9D8F)
val WarmText = Color(0xFF242725)
val LightText = Color(0xFFF1F3F4)

@Composable
fun NovaForgeTheme(
    themeMode: String = "warm-light",
    accentColor: String = "#2A9D8F",
    content: @Composable () -> Unit
) {
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(accentColor))
    } catch (_: Exception) {
        DefaultTeal
    }

    val colors = when (themeMode) {
        "charcoal-dark" -> darkColorScheme(
            primary = primaryColor,
            background = DarkBackground,
            surface = Color(0xFF1E2024),
            surfaceVariant = Color(0xFF282C34),
            onBackground = LightText,
            onSurface = LightText
        )
        "monochrome" -> darkColorScheme(
            primary = Color.White,
            background = Color(0xFF000000),
            surface = Color(0xFF121212),
            surfaceVariant = Color(0xFF222222),
            onBackground = Color.White,
            onSurface = Color.White
        )
        else -> lightColorScheme(
            primary = primaryColor,
            background = LightBackground,
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFECE6),
            onBackground = WarmText,
            onSurface = WarmText
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
