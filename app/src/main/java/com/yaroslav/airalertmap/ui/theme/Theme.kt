package com.airalertmap.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AirAlertDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB74D),
    onPrimary = Color(0xFF1A1200),
    primaryContainer = Color(0xFF5B3F00),
    onPrimaryContainer = Color(0xFFFFDDB0),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF0D1C10),
    secondaryContainer = Color(0xFF204126),
    onSecondaryContainer = Color(0xFFC7E8CA),
    tertiary = Color(0xFF64B5F6),
    onTertiary = Color(0xFF061723),
    tertiaryContainer = Color(0xFF16344A),
    onTertiaryContainer = Color(0xFFCAE6FF),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE8EDF2),
    surface = Color(0xFF11161D),
    onSurface = Color(0xFFE8EDF2),
    surfaceVariant = Color(0xFF1B2330),
    onSurfaceVariant = Color(0xFFB2BCC8),
    outline = Color(0xFF314052),
    outlineVariant = Color(0xFF232B36),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE8EDF2),
    inverseOnSurface = Color(0xFF11161D),
    inversePrimary = Color(0xFF8A5C00),
    surfaceTint = Color(0xFFFFB74D),
    surfaceBright = Color(0xFF1A2028),
    surfaceContainerLowest = Color(0xFF0B0F14),
    surfaceContainerLow = Color(0xFF11161D),
    surfaceContainer = Color(0xFF151B23),
    surfaceContainerHigh = Color(0xFF1A2028),
    surfaceContainerHighest = Color(0xFF202733)
)

@Composable
fun AirAlertTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AirAlertDarkColorScheme,
        content = content
    )
}
