package de.veloce.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

object VeloceColors {
    val Background = Color(0xFFF6F6F3)
    val Surface = Color.White
    val Ink = Color(0xFF171717)
    val Muted = Color(0xFF888985)
    val Line = Color(0xFFECECE8)
    val Orange = Color(0xFFE58A45)
    val PaleOrange = Color(0xFFFFF0E4)
    val PaleGreen = Color(0xFFEAF4EC)
    val Green = Color(0xFF39744B)
    val Error = Color(0xFFB42318)
}

private val LightColors = lightColorScheme(
    primary = VeloceColors.Ink,
    onPrimary = Color.White,
    secondary = VeloceColors.Orange,
    background = VeloceColors.Background,
    surface = VeloceColors.Surface,
    onSurface = VeloceColors.Ink,
    error = VeloceColors.Error,
)

private val VeloceTypography = Typography(
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.SansSerif),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
    ),
)

private val VeloceShapes = Shapes(
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun VeloceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = VeloceTypography,
        shapes = VeloceShapes,
        content = content,
    )
}
