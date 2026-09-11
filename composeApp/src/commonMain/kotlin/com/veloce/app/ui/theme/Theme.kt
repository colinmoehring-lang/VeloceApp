package com.veloce.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBg = Color(0xFF0F111A)
val CardBg = Color(0xFF1B1E2E)
val CardBorder = Color(0xFF2A2E45)

val NeonCyan = Color(0xFF00E5FF)
val ElectricLime = Color(0xFF39FF14)
val NeonOrange = Color(0xFFFF5252)
val NeonPurple = Color(0xFF9D4EDD)

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = ElectricLime,
    tertiary = NeonPurple,
    background = DarkBg,
    surface = CardBg,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun VeloceTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
