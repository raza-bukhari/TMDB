package com.example.tmdb.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.tmdb.core.designsystem.R

/**
 * Headers: Bebas Neue — a condensed display face that ships a single weight;
 * bold/black header looks come from its inherent design, so weight stays Normal.
 * Body/labels: Poppins 400–500 per the app's font spec.
 */
private val BebasNeue = FontFamily(
    Font(R.font.bebas_neue_regular, FontWeight.Normal),
)

private val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

val Typography = Typography(
    // Display/headline/title-large: Bebas Neue. Condensed caps need looser tracking and tighter leading.
    displayLarge = TextStyle(fontFamily = BebasNeue, fontSize = 52.sp, lineHeight = 56.sp, letterSpacing = 1.sp),
    displayMedium = TextStyle(fontFamily = BebasNeue, fontSize = 42.sp, lineHeight = 46.sp, letterSpacing = 1.sp),
    displaySmall = TextStyle(fontFamily = BebasNeue, fontSize = 34.sp, lineHeight = 38.sp, letterSpacing = 0.75.sp),
    headlineLarge = TextStyle(fontFamily = BebasNeue, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = 0.75.sp),
    headlineMedium = TextStyle(fontFamily = BebasNeue, fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = 0.5.sp),
    headlineSmall = TextStyle(fontFamily = BebasNeue, fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = 0.5.sp),
    titleLarge = TextStyle(fontFamily = BebasNeue, fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = 0.5.sp),

    // Body & labels: Poppins 400–500.
    titleMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    labelLarge = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelSmall = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
)
