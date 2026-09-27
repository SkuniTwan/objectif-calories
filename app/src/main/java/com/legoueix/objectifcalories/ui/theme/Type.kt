package com.legoueix.objectifcalories.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.legoueix.objectifcalories.R

// Police unique du design "Bento assiette" (Bricolage Grotesque, Google Fonts, SIL OFL).
val BricolageGrotesque = FontFamily(
    Font(R.font.bricolage_grotesque_regular, FontWeight.Normal),
    Font(R.font.bricolage_grotesque_medium, FontWeight.Medium),
    Font(R.font.bricolage_grotesque_semibold, FontWeight.SemiBold),
    Font(R.font.bricolage_grotesque_bold, FontWeight.Bold),
    Font(R.font.bricolage_grotesque_extrabold, FontWeight.ExtraBold),
)

// Tailles et interlignes repris de l'échelle Material3 par défaut (inchangés pour ne
// pas perturber les mises en page existantes) ; seules la police et les graisses
// changent, alignées sur le design ("les valeurs numériques sont toujours en 800").
val Typography = Typography(
    displayLarge = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.ExtraBold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.ExtraBold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = BricolageGrotesque, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp),
)
