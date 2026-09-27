package com.legoueix.objectifcalories.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    secondary = LightYellow,
    background = OffWhite,
    surface = Color.White,
    onBackground = Charcoal,
    onSurface = Charcoal,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = TextSecondary,
    error = Red,
)

private val DarkColors = darkColorScheme(
    primary = Terracotta,
    onPrimary = Color.White,
    secondary = LightYellow,
    background = Charcoal,
    surface = Charcoal,
)

// Rayons du design "Bento assiette" : tuile standard 22-24px (forme par défaut des
// Card sans shape explicite), tuile principale 26-28px, tuile secondaire 18-20px,
// ligne de liste 16px, feuille montante 30px.
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(20.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(30.dp),
)

@Composable
fun ObjectifCaloriesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content,
    )
}
