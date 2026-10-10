package com.ven.app.ui.theme

import androidx.compose.ui.graphics.Color

val CategoryColorMap: Map<String, Color> = mapOf(
    "3D" to Color(0xFFFF9A00),
    "2D" to Color(0xFFFF1F00),
    "Animation" to Color(0xFF7ABF00),
    "Game Dev" to Color(0xFF2A0FD6),
    "Cybersecurity" to Color(0xFF0E7C66),
    "Software" to Color(0xFF1565C0),
    "UI/UX" to Color(0xFFD81B8C),
    "Videography" to Color(0xFF00838F),
    "Photography" to Color(0xFF6D4C41),
    "Internet Of Things" to Color(0xFF546E7A),
    "Automation System" to Color(0xFF7B5E00),
    "Fabrication" to Color(0xFF9E4A00),
    "Manufacturing" to Color(0xFF37474F),
    "Others" to Color(0xFF616161),
)

fun onCategoryColor(bg: Color): Color {
    val luminance = 0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue
    return if (luminance > 0.5f) Color.Black else Color.White
}
