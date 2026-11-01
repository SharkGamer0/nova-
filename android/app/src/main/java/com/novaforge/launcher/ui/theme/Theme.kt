package com.novaforge.launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Semantic Theme Palette
// Dark tokens
val CosmicBackground = Color(0xFF07090E)      // Deep navy/near-black
val MidnightSurface = Color(0xFF0D1117)       // Midnight surface
val CosmicSurfaceVariant = Color(0xFF151B26)  // Elevated surface/card
val CosmicCyan = Color(0xFF38BDF8)            // Primary accent: subtle cyan
val CosmicPurple = Color(0xFF6366F1)          // Secondary accent: restrained indigo/purple
val SoftGrey = Color(0xFF94A3B8)              // Celestial soft grey (secondary text)
val DarkMutedText = Color(0xFF64748B)         // Muted/caption text
val DarkPrimaryText = Color(0xFFF8FAFC)       // Crisp white text
val DarkBorder = Color(0x26FFFFFF)            // Subtle white border (15%)

// Light tokens
val LightBackground = Color(0xFFF8F9FA)       // Crisp soft white/grey
val LightSurface = Color(0xFFFFFFFF)          // Pure white surface
val LightSurfaceVariant = Color(0xFFF1F3F5)   // Subtle light grey container
val LightPrimaryText = Color(0xFF1E293B)      // Dark charcoal/slate
val LightSecondaryText = Color(0xFF475569)    // Readable medium grey
val LightMutedText = Color(0xFF64748B)        // Light muted text
val LightBorder = Color(0x24000000)           // Subtle dark border (14%)

@Composable
fun NovaForgeTheme(
    themeMode: String = "system",
    accentColor: String = "#38BDF8",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode.lowercase()) {
        "system", "follow-system" -> systemDark
        "dark", "cosmic-dark", "charcoal-dark", "monochrome" -> true
        "light", "warm-light" -> false
        else -> systemDark
    }

    val primaryColor = try {
        Color(android.graphics.Color.parseColor(accentColor))
    } catch (_: Exception) {
        CosmicCyan
    }

    val colors = if (isDark) {
        if (themeMode == "monochrome") {
            darkColorScheme(
                primary = Color.White,
                secondary = Color(0xFFCCCCCC),
                tertiary = Color(0xFFAAAAAA),
                background = Color(0xFF000000),
                surface = Color(0xFF121212),
                surfaceVariant = Color(0xFF222222),
                onBackground = Color.White,
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFF888888),
                outline = Color(0x33FFFFFF)
            )
        } else {
            darkColorScheme(
                primary = primaryColor,
                secondary = CosmicPurple,
                tertiary = CosmicCyan,
                background = CosmicBackground,
                surface = MidnightSurface,
                surfaceVariant = CosmicSurfaceVariant,
                onBackground = DarkPrimaryText,
                onSurface = DarkPrimaryText,
                onSurfaceVariant = SoftGrey,
                outline = DarkBorder
            )
        }
    } else {
        lightColorScheme(
            primary = primaryColor,
            secondary = CosmicPurple,
            tertiary = CosmicCyan,
            background = LightBackground,
            surface = LightSurface,
            surfaceVariant = LightSurfaceVariant,
            onBackground = LightPrimaryText,
            onSurface = LightPrimaryText,
            onSurfaceVariant = LightSecondaryText,
            outline = LightBorder
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
