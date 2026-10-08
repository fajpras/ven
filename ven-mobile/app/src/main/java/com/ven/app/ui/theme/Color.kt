package com.ven.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class VexColors(
    val bg: Color,
    val surface: Color,
    val navBar: Color,
    val surfaceInput: Color,
    val border: Color,
    val borderInput: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryText: Color,
    val primaryDisabled: Color,
    val danger: Color,
    val onDanger: Color,
    val success: Color,
    val inverseBg: Color,
    val inverseText: Color,
    val scrim: Color,
)

val BrandPurple = Color(0xFFBA18F5)

val DarkVexColors = VexColors(
    bg = Color(0xFF1F1F1F),
    navBar = Color(0xFF252525),
    surface = Color(0xFF252525),
    surfaceInput = Color.White.copy(alpha = 0.10f),
    border = Color.White.copy(alpha = 0.10f),
    borderInput = Color.White.copy(alpha = 0.20f),
    text = Color.White,
    textSecondary = Color.White.copy(alpha = 0.60f),
    textMuted = Color.White.copy(alpha = 0.40f),
    textDisabled = Color.White.copy(alpha = 0.25f),
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryText = Color(0xFFD257FF),
    primaryDisabled = BrandPurple.copy(alpha = 0.28f),
    danger = Color(0xFFFF0000),
    onDanger = Color.White,
    success = Color(0xFF00E85C),
    inverseBg = Color.White,
    inverseText = Color(0xFF1F1F1F),
    scrim = Color.Black.copy(alpha = 0.60f),
)

val LightVexColors = VexColors(
    bg = Color.White,
    surface = Color.White,
    navBar = Color.White,
    surfaceInput = Color(0xFFE5E5E5),
    border = Color(0xFFDADADA),
    borderInput = Color(0xFFBDBDBD),
    text = Color.Black,
    textSecondary = Color(0xFF5C5C63),
    textMuted = Color(0xFF8A8A92),
    textDisabled = Color(0xFFB5B5BD),
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryText = BrandPurple,
    primaryDisabled = BrandPurple.copy(alpha = 0.25f),
    danger = Color(0xFFFF0000),
    onDanger = Color.White,
    success = Color(0xFF00A843),
    inverseBg = Color.Black,
    inverseText = Color.White,
    scrim = Color.Black.copy(alpha = 0.50f),
)
