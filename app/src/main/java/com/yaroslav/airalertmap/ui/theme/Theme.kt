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

// Material 3 Expressive — deep navy/teal with vibrant accents
private val AirAlertDarkColorScheme = darkColorScheme(
    primary           = Color(0xFF7DD3E8),   // sky blue — main accent
    onPrimary         = Color(0xFF003543),
    primaryContainer  = Color(0xFF004D60),
    onPrimaryContainer= Color(0xFFB8EAFA),
    secondary         = Color(0xFFFFB869),   // warm amber
    onSecondary       = Color(0xFF462A00),
    secondaryContainer= Color(0xFF623D00),
    onSecondaryContainer= Color(0xFFFFDDB6),
    tertiary          = Color(0xFF6DE89A),   // green — "safe / online"
    onTertiary        = Color(0xFF003919),
    tertiaryContainer = Color(0xFF005227),
    onTertiaryContainer= Color(0xFF8FFBB4),
    error             = Color(0xFFFF6B6B),   // alert red — more vibrant
    onError           = Color(0xFF690005),
    errorContainer    = Color(0xFF93000A),
    onErrorContainer  = Color(0xFFFFDAD6),
    background        = Color(0xFF090E13),   // deep dark
    onBackground      = Color(0xFFDDE4E8),
    surface           = Color(0xFF0E1419),
    onSurface         = Color(0xFFDDE4E8),
    surfaceVariant    = Color(0xFF1E2C35),
    onSurfaceVariant  = Color(0xFFB0BEC7),
    outline           = Color(0xFF4A5C66),
    outlineVariant    = Color(0xFF2A3840),
    scrim             = Color(0xFF000000),
    inverseSurface    = Color(0xFFDDE4E8),
    inverseOnSurface  = Color(0xFF0E1419),
    inversePrimary    = Color(0xFF006880),
    surfaceTint       = Color(0xFF7DD3E8),
    surfaceBright     = Color(0xFF1E2C35),
    surfaceContainerLowest  = Color(0xFF05090C),
    surfaceContainerLow     = Color(0xFF0E1419),
    surfaceContainer        = Color(0xFF131B21),
    surfaceContainerHigh    = Color(0xFF1A2530),
    surfaceContainerHighest = Color(0xFF213039)
)

private val AirAlertTypography = Typography().let { base ->
    base.copy(
        displaySmall = base.displaySmall.copy(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        ),
        headlineLarge = base.headlineLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        ),
        headlineMedium = base.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp
        ),
        headlineSmall = base.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp
        ),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.Medium)
    )
}

// M3 Expressive — more generous radii
private val AirAlertShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small      = RoundedCornerShape(16.dp),
    medium     = RoundedCornerShape(24.dp),
    large      = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp)
)

@Composable
fun AirAlertTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AirAlertDarkColorScheme,
        typography  = AirAlertTypography,
        shapes      = AirAlertShapes,
        content     = content
    )
}
