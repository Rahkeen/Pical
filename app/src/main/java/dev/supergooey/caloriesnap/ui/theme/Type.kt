package dev.supergooey.caloriesnap.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.supergooey.caloriesnap.R

val plex = FontFamily(
  Font(
    resId = R.font.ibm_plex_regular,
    weight = FontWeight.Normal
  ),
  Font(
    resId = R.font.ibm_plex_medium,
    weight = FontWeight.Medium
  ),
  Font(
    resId = R.font.ibm_plex_bold,
    weight = FontWeight.Bold
  ),
)

// Set of Material typography styles to start with
val Typography = Typography(
  displaySmall = TextStyle(
    fontFamily = plex,
    fontWeight = FontWeight.Bold,
    letterSpacing = 1.sp,
    fontSize = 12.sp
  ),
  displayMedium = TextStyle(
    fontFamily = plex,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp
  ),
  displayLarge = TextStyle(
    fontFamily = plex,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp
  ),
  /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)