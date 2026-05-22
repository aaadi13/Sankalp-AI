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
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ProfDarkPrimary,
    onPrimary = ProfDarkBg,
    primaryContainer = ProfDarkSurfaceVariant,
    onPrimaryContainer = ProfDarkPrimary,
    secondary = ProfSecondary,
    onSecondary = ProfDarkBg,
    secondaryContainer = ProfDarkSurfaceVariant,
    onSecondaryContainer = ProfSecondary,
    tertiary = ProfTertiary,
    onTertiary = ProfDarkOnSurface,
    background = ProfDarkBg,
    onBackground = ProfDarkOnSurface,
    surface = ProfDarkSurface,
    onSurface = ProfDarkOnSurface,
    surfaceVariant = ProfDarkSurfaceVariant,
    onSurfaceVariant = ProfDarkOnSurfaceVariant,
    error = ProfError,
    onError = ProfDarkBg,
    errorContainer = ProfDarkSurfaceVariant,
    onErrorContainer = ProfError,
    outline = ProfDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = ProfPrimary,
    onPrimary = Color.White,
    primaryContainer = ProfPrimaryContainer,
    onPrimaryContainer = ProfOnPrimaryContainer,
    secondary = ProfSecondary,
    onSecondary = Color.White,
    secondaryContainer = ProfSecondaryContainer,
    onSecondaryContainer = ProfOnSecondaryContainer,
    tertiary = ProfTertiary,
    onTertiary = Color.White,
    background = ProfLightBg,
    onBackground = ProfOnSurface,
    surface = ProfSurface,
    onSurface = ProfOnSurface,
    surfaceVariant = ProfSurfaceVariant,
    onSurfaceVariant = ProfOnSurfaceVariant,
    error = ProfError,
    onError = Color.White,
    errorContainer = ProfErrorContainer,
    onErrorContainer = ProfOnErrorContainer,
    outline = ProfOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to enforce our high-quality custom brand palette!
    content: @Composable () -> Unit,
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
