package com.novaforge.launcher.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Minimalist Space & Cosmic Palette
val CosmicBackground = Color(0xFF07090E)      // Near-black cosmic void
val MidnightSurface = Color(0xFF0D1117)       // Midnight blue surface
val CosmicSurfaceVariant = Color(0xFF151B26)  // Muted cosmic surface border
val CosmicCyan = Color(0xFF38BDF8)            // Primary accent: subtle cyan
val CosmicPurple = Color(0xFF6366F1)          // Secondary accent: restrained indigo/purple
val SoftGrey = Color(0xFF94A3B8)              // Celestial soft grey
val LightText = Color(0xFFF1F3F4)             // Crisp white/light text
val WarmText = Color(0xFF242725)
val LightBackground = Color(0xFFF7F6F2)

@Composable
fun NovaForgeTheme(
    themeMode: String = "cosmic-dark",
    accentColor: String = "#38BDF8",
    content: @Composable () -> Unit
) {
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(accentColor))
    } catch (_: Exception) {
        CosmicCyan
    }

    val colors = when (themeMode) {
        "cosmic-dark", "charcoal-dark" -> darkColorScheme(
            primary = primaryColor,
            secondary = CosmicPurple,
            tertiary = CosmicCyan,
            background = CosmicBackground,
            surface = MidnightSurface,
            surfaceVariant = CosmicSurfaceVariant,
            onBackground = LightText,
            onSurface = LightText,
            onSurfaceVariant = SoftGrey
        )
        "monochrome" -> darkColorScheme(
            primary = Color.White,
            secondary = Color(0xFFCCCCCC),
            tertiary = Color(0xFFAAAAAA),
            background = Color(0xFF000000),
            surface = Color(0xFF121212),
            surfaceVariant = Color(0xFF222222),
            onBackground = Color.White,
            onSurface = Color.White,
            onSurfaceVariant = Color(0xFF888888)
        )
        else -> lightColorScheme(
            primary = primaryColor,
            secondary = CosmicPurple,
            tertiary = CosmicCyan,
            background = LightBackground,
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFECE6),
            onBackground = WarmText,
            onSurface = WarmText,
            onSurfaceVariant = Color(0xFF5A605B)
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
