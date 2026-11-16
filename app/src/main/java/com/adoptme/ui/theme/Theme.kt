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


val CoralPrimary = Color(0xFFD9534F)
val CoralDeep = Color(0xFFB23B37)
val CoralSoft = Color(0xFFF7E1DF)
val CoralAccent = CoralPrimary
val Honey = Color(0xFFE0A03A)
val HoneySoft = Color(0xFFFCEFD8)
val Mint = Color(0xFF2F9E6B)
val MintSoft = Color(0xFFDDF5EA)
val Ink = Color(0xFF1E1B1A)
val InkSoft = Color(0xFF6F6864)
val WarmCloud = Color(0xFFF7F5F3)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceMuted = Color(0xFFF1EDEA)
val OutlineSoft = Color(0xFFE5DFDB)
val OutlineFaint = Color(0xFFF0EBE8)
val DangerRed = Color(0xFFC0392B)
val WarningAmber = Color(0xFFF2A93B)

val ReportAbandonment = WarningAmber
val ReportDanger = DangerRed
val ReportAdoption = CoralPrimary

private val LightColors = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralSoft,
    onPrimaryContainer = Color(0xFF5A1F1C),
    secondary = CoralDeep,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4D6D4),
    onSecondaryContainer = Color(0xFF5A1F1C),
    tertiary = Honey,
    onTertiary = Color.White,
    tertiaryContainer = HoneySoft,
    onTertiaryContainer = Color(0xFF5A3A0A),
    background = WarmCloud,
    onBackground = Ink,
    surface = SurfaceWhite,
    onSurface = Ink,
    surfaceVariant = SurfaceMuted,
    onSurfaceVariant = InkSoft,
    outline = OutlineSoft,
    outlineVariant = OutlineFaint,
    error = DangerRed,
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    inverseSurface = Ink,
    inverseOnSurface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF08A84),
    onPrimary = Color(0xFF4A0F0D),
    primaryContainer = Color(0xFF6B2420),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFFFB4AB),
    onSecondary = Color(0xFF561E1A),
    secondaryContainer = Color(0xFF73332F),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFF2C078),
    onTertiary = Color(0xFF442B00),
    tertiaryContainer = Color(0xFF624000),
    onTertiaryContainer = Color(0xFFFFDFA0),
    background = Color(0xFF121010),
    onBackground = Color(0xFFF5F0EE),
    surface = Color(0xFF1C1918),
    onSurface = Color(0xFFF5F0EE),
    surfaceVariant = Color(0xFF2A2624),
    onSurfaceVariant = Color(0xFFB8AFA9),
    outline = Color(0xFF4A4441),
    outlineVariant = Color(0xFF332E2C),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFF5F0EE),
    inverseOnSurface = Ink
)


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
