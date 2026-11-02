package com.adoptme.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// -------------------------------------------------------------------------
// PALETA "OLIVA & ARCILLA"
// -------------------------------------------------------------------------
val OlivePrimary = Color(0xFF5A6B3B)
val OliveDeep = Color(0xFF3F4A2A)
val OliveSoft = Color(0xFFE6EAD6)
val Clay = Color(0xFFA85A34)
val ClaySoft = Color(0xFFF7E2D7)
val Honey = Color(0xFFC9A65C)
val Cream = Color(0xFFFAF9F3)
val SurfaceMuted = Color(0xFFEFEEE4)
val Ink = Color(0xFF1D2318)
val InkSoft = Color(0xFF666E5C)
val OutlineSoft = Color(0xFFD9D7C7)
val SuccessGreen = Color(0xFF2F7A5A)
val WarningAmber = Color(0xFFC98A2E)
val DangerRed = Color(0xFFB4433A)

// Semántica del mapa de reportes
val ReportAbandonment = WarningAmber
val ReportDanger = DangerRed
val ReportAdoption = OlivePrimary

private val LightColors = lightColorScheme(
    primary = OlivePrimary,
    onPrimary = Color.White,
    primaryContainer = OliveSoft,
    onPrimaryContainer = Color(0xFF2B341A),
    secondary = Clay,
    onSecondary = Color.White,
    secondaryContainer = ClaySoft,
    onSecondaryContainer = Color(0xFF3B1507),
    tertiary = Honey,
    onTertiary = Color.White,
    background = Cream,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = InkSoft,
    outline = OutlineSoft,
    error = DangerRed,
    onError = Color.White,
    errorContainer = Color(0xFFFBE0DC),
    onErrorContainer = Color(0xFF6E1D16)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBCC894),
    onPrimary = Color(0xFF2A3316),
    primaryContainer = Color(0xFF404D26),
    onPrimaryContainer = Color(0xFFE4EAD3),
    secondary = Color(0xFFE8A47F),
    onSecondary = Color(0xFF4A1F0E),
    secondaryContainer = Color(0xFF5A2A15),
    onSecondaryContainer = Color(0xFFFFDACC),
    tertiary = Color(0xFFE5C88A),
    onTertiary = Color(0xFF3B2E0B),
    background = Color(0xFF11150F),
    onBackground = Color(0xFFECEFE2),
    surface = Color(0xFF1A1F17),
    onSurface = Color(0xFFECEFE2),
    surfaceVariant = Color(0xFF242A20),
    onSurfaceVariant = Color(0xFFA8B098),
    outline = Color(0xFF3C4232),
    error = Color(0xFFE5847B),
    onError = Color(0xFF4A0F0B),
    errorContainer = Color(0xFF6E1D16),
    onErrorContainer = Color(0xFFFFDAD5)
)

// -------------------------------------------------------------------------
// TIPOGRAFÍA (escala base sobre la familia del sistema)
// -------------------------------------------------------------------------
private val AdoptMeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

private val AdoptMeShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun AdoptMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AdoptMeTypography,
        shapes = AdoptMeShapes,
        content = content
    )
}
