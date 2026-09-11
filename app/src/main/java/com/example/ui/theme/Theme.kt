package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.model.AppThemeMode
import kotlinx.coroutines.delay
import java.util.Calendar

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = EmeraldSecondary,
    onSecondary = EmeraldOnSecondary,
    secondaryContainer = Color(0xFF075985),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = EmeraldTertiary,
    onTertiary = EmeraldOnTertiary,
    tertiaryContainer = Color(0xFF312E81),
    onTertiaryContainer = Color(0xFFE0E7FF),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = EmeraldSecondary,
    onSecondary = EmeraldOnSecondary,
    secondaryContainer = EmeraldSecondaryContainer,
    onSecondaryContainer = EmeraldOnSecondaryContainer,
    tertiary = EmeraldTertiary,
    onTertiary = EmeraldOnTertiary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFE2E8F0)
)

/**
 * Calculates whether dark theme should be active based on the AppThemeMode.
 * In AUTO mode:
 * - Daytime (06:00 to 17:59): Light theme (unless system dark mode is active)
 * - Nighttime (18:00 to 05:59): Dark theme
 * - Automatically checks every 15 seconds so transitions occur smoothly without requiring an app restart.
 */
@Composable
fun rememberIsDarkTheme(themeMode: AppThemeMode): Boolean {
    val systemInDark = isSystemInDarkTheme()
    var currentHour by remember {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    }

    LaunchedEffect(themeMode) {
        if (themeMode == AppThemeMode.AUTO) {
            while (true) {
                currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                delay(15_000)
            }
        }
    }

    return when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.AUTO -> {
            val isNightTime = currentHour < 6 || currentHour >= 18
            isNightTime || systemInDark
        }
    }
}

@Composable
fun WiChatTheme(
    themeMode: AppThemeMode = AppThemeMode.AUTO,
    darkTheme: Boolean = rememberIsDarkTheme(themeMode),
    dynamicColor: Boolean = false, // Use intentional WiChat branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for template compatibility if needed
@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.AUTO,
    darkTheme: Boolean = rememberIsDarkTheme(themeMode),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WiChatTheme(themeMode = themeMode, darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

