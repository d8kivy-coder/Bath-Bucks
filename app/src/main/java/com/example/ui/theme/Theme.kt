package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BathtimeColorScheme =
  darkColorScheme(
    primary = DuckGold,
    onPrimary = BathDeepNavy,
    secondary = BathTileCyan,
    onSecondary = BathDeepNavy,
    tertiary = ArcadeGreenBright,
    onTertiary = Color.White,
    background = BathDeepNavy,
    onBackground = BubbleWhite,
    surface = CabinetBezelDark,
    onSurface = BubbleWhite,
    surfaceVariant = BathRoyalBlue,
    onSurfaceVariant = Color(0xFFBAE6FD),
    error = ArcadeRed,
    onError = Color.White,
  )

@Composable
fun BathtimeBucksTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = BathtimeColorScheme, typography = Typography, content = content)
}
