package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalSeniorMode = compositionLocalOf { false }

val SatarkShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

private val SatarkColorScheme = lightColorScheme(
    primary = SatarkAccent,
    onPrimary = Color.White,
    primaryContainer = SatarkPanelCard,
    onPrimaryContainer = SatarkInk,
    secondary = SatarkOk,
    onSecondary = Color.White,
    secondaryContainer = SatarkOkAlpha,
    onSecondaryContainer = SatarkOk,
    tertiary = SatarkPurple,
    onTertiary = Color.White,
    tertiaryContainer = SatarkPurpleAlpha,
    onTertiaryContainer = SatarkPurple,
    error = SatarkDanger,
    onError = Color.White,
    errorContainer = SatarkDangerAlpha,
    onErrorContainer = SatarkDanger,
    background = SatarkBg,
    onBackground = SatarkInk,
    surface = SatarkPanel,
    onSurface = SatarkInk,
    surfaceVariant = SatarkPanelCard,
    onSurfaceVariant = SatarkDim,
    outline = SatarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    seniorMode: Boolean = false,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSeniorMode provides seniorMode) {
        MaterialTheme(
            colorScheme = SatarkColorScheme,
            typography = if (seniorMode) SeniorTypography else Typography,
            shapes = SatarkShapes,
            content = content
        )
    }
}
