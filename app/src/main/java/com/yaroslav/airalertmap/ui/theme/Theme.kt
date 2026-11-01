package com.airalertmap.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AirAlertDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB7D9E5),
    onPrimary = Color(0xFF153640),
    primaryContainer = Color(0xFF2C4E59),
    onPrimaryContainer = Color(0xFFD3F0F7),
    secondary = Color(0xFFF2C66D),
    onSecondary = Color(0xFF3E2E00),
    secondaryContainer = Color(0xFF5A4612),
    onSecondaryContainer = Color(0xFFFFE9AE),
    tertiary = Color(0xFF9CD6A7),
    onTertiary = Color(0xFF12381D),
    tertiaryContainer = Color(0xFF28502F),
    onTertiaryContainer = Color(0xFFB8F2C2),
    background = Color(0xFF10151A),
    onBackground = Color(0xFFE1E7EA),
    surface = Color(0xFF151B20),
    onSurface = Color(0xFFE1E7EA),
    surfaceVariant = Color(0xFF29343A),
    onSurfaceVariant = Color(0xFFBFC9CD),
    outline = Color(0xFF52616A),
    outlineVariant = Color(0xFF39464C),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFE1E7EA),
    inverseOnSurface = Color(0xFF151B20),
    inversePrimary = Color(0xFF42636E),
    surfaceTint = Color(0xFFB7D9E5),
    surfaceBright = Color(0xFF29343A),
    surfaceContainerLowest = Color(0xFF0B1014),
    surfaceContainerLow = Color(0xFF151B20),
    surfaceContainer = Color(0xFF1A2228),
    surfaceContainerHigh = Color(0xFF222C32),
    surfaceContainerHighest = Color(0xFF2B363C)
)

private val AirAlertTypography = Typography().let { base ->
    base.copy(
        headlineSmall = base.headlineSmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.4).sp
        ),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.Medium)
    )
}

private val AirAlertShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

@Composable
fun AirAlertTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AirAlertDarkColorScheme,
        typography = AirAlertTypography,
        shapes = AirAlertShapes,
        content = content
    )
}
