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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.adoptme.R

// -------------------------------------------------------------------------
// PALETA "AZUL & NARANJA" — extraída de las referencias visuales
// Azul eléctrico, naranja cálido, tinta profunda y superficies limpias.
// -------------------------------------------------------------------------

val BluePrimary = Color(0xFF2F80ED)
val BlueDeep = Color(0xFF1357B8)
val BlueSoft = Color(0xFFE3EEFF)
val OrangeAccent = Color(0xFFF47B2A)
val OrangeSoft = Color(0xFFFFE8D8)
val Mint = Color(0xFF2F9E6B)
val MintSoft = Color(0xFFDDF5EA)
val Ink = Color(0xFF111318)
val InkSoft = Color(0xFF667085)
val Cloud = Color(0xFFF4F6FA)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceMuted = Color(0xFFE9EDF4)
val OutlineSoft = Color(0xFFE1E6EF)
val OutlineFaint = Color(0xFFEEF1F6)
val DangerRed = Color(0xFFE5484D)
val WarningAmber = Color(0xFFF2A93B)

// Semántica del mapa de reportes
val ReportAbandonment = WarningAmber
val ReportDanger = DangerRed
val ReportAdoption = BluePrimary

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    onPrimary = Color.White,
    primaryContainer = BlueSoft,
    onPrimaryContainer = Color(0xFF123B73),
    secondary = OrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = OrangeSoft,
    onSecondaryContainer = Color(0xFF5A2A0C),
    tertiary = Mint,
    onTertiary = Color.White,
    tertiaryContainer = MintSoft,
    onTertiaryContainer = Color(0xFF0C3B29),
    background = Cloud,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = InkSoft,
    outline = OutlineSoft,
    outlineVariant = OutlineFaint,
    error = DangerRed,
    onError = Color.White,
    errorContainer = Color(0xFFFDE4E5),
    onErrorContainer = Color(0xFF6E1D1F),
    inverseSurface = Ink,
    inverseOnSurface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AB4FF),
    onPrimary = Color(0xFF0A2A55),
    primaryContainer = Color(0xFF1D3E6B),
    onPrimaryContainer = Color(0xFFD9E6FF),
    secondary = Color(0xFFFFB184),
    onSecondary = Color(0xFF4A1F08),
    secondaryContainer = Color(0xFF5A2A12),
    onSecondaryContainer = Color(0xFFFFE0CC),
    tertiary = Color(0xFF7ED9B0),
    onTertiary = Color(0xFF063A28),
    tertiaryContainer = Color(0xFF0F4A35),
    onTertiaryContainer = Color(0xFFD7F7E8),
    background = Color(0xFF0B0E13),
    onBackground = Color(0xFFF1F4F8),
    surface = Color(0xFF151A22),
    onSurface = Color(0xFFF1F4F8),
    surfaceVariant = Color(0xFF1E2530),
    onSurfaceVariant = Color(0xFFA7B0C0),
    outline = Color(0xFF303846),
    outlineVariant = Color(0xFF232A35),
    error = Color(0xFFFF8A8A),
    onError = Color(0xFF4A0C0E),
    errorContainer = Color(0xFF5C1A1D),
    onErrorContainer = Color(0xFFFFDAD9),
    inverseSurface = Color(0xFFF1F4F8),
    inverseOnSurface = Color(0xFF111318)
)

// -------------------------------------------------------------------------
// TIPOGRAFÍA — Poppins, geométrica y redondeada como las referencias
// -------------------------------------------------------------------------

val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private val AdoptMeTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-0.8).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.6).sp
    ),
    displaySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.2).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

private val AdoptMeShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
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
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AdoptMeTypography,
        shapes = AdoptMeShapes,
        content = content
    )
}
