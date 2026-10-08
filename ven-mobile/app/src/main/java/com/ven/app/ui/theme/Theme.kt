package com.ven.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class ThemeMode { Dark, Light, System }

val LocalVexColors = staticCompositionLocalOf { DarkVexColors }

@Composable
fun VexTheme(mode: ThemeMode = ThemeMode.Dark, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
        ThemeMode.System -> isSystemInDarkTheme()
    }
    val vex = if (dark) DarkVexColors else LightVexColors
    val scheme = (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = vex.primary,
        onPrimary = vex.onPrimary,
        background = vex.bg,
        onBackground = vex.text,
        surface = vex.surface,
        onSurface = vex.text,
        outline = vex.border,
        error = vex.danger,
        onError = vex.onDanger,
        scrim = vex.scrim,
    )
    CompositionLocalProvider(LocalVexColors provides vex) {
        MaterialTheme(
            colorScheme = scheme,
            typography = VexTypography,
            shapes = VexShapes,
            content = content,
        )
    }
}

object VexTheme {
    val colors: VexColors
        @Composable get() = LocalVexColors.current
}
