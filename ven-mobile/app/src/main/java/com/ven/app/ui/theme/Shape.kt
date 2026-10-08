package com.ven.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object VexRadius {
    val xs = 4.dp
    val md = 8.dp
    val lg = 16.dp
    val xl = 28.dp
    val full = 100.dp
}

val VexShapes = Shapes(
    extraSmall = RoundedCornerShape(VexRadius.xs),
    small = RoundedCornerShape(VexRadius.md),
    medium = RoundedCornerShape(VexRadius.lg),
    large = RoundedCornerShape(VexRadius.xl),
    extraLarge = RoundedCornerShape(VexRadius.full),
)
