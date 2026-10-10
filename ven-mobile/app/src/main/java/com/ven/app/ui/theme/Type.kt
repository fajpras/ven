package com.ven.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Font Family Elms Sans sesuai DESIGN.md §4.1.
 * Menggunakan fallback [FontFamily.SansSerif] bila file font statis res/font belum dimuat.
 */
val ElmsSans = FontFamily.SansSerif

val VexTypography = Typography(
    displayMedium = TextStyle(
        fontFamily = ElmsSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = ElmsSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ElmsSans,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = ElmsSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = ElmsSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ElmsSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = ElmsSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = ElmsSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
)

val ChatBodyStyle = TextStyle(
    fontFamily = ElmsSans,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 25.2.sp,
)
