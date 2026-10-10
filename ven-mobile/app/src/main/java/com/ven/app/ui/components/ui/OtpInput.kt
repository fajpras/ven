package com.ven.app.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Komponen input OTP numerik dengan kotak terpisah.
 *
 * Sesuai spesifikasi DESIGN.md §6.1 & §8.9:
 * - [digitCount] digit kotak (default 6 digit, selaras dengan API backend).
 * - Radius sudut 8dp ([VexRadius.md]).
 * - Latar belakang [VexTheme.colors.surfaceInput] dan outline [VexTheme.colors.borderInput].
 * - State fokus: outline [VexTheme.colors.primary] 2dp.
 * - State error: outline [VexTheme.colors.danger] 1.5dp.
 * - Mendukung paste teks, auto-advance, dan keypad angka.
 */
@Composable
fun OtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    digitCount: Int = 6,
    isError: Boolean = false,
    enabled: Boolean = true,
    boxWidth: Dp = 46.dp,
    boxHeight: Dp = 54.dp,
    onOtpComplete: ((String) -> Unit)? = null,
) {
    val colors = VexTheme.colors
    val focusRequester = remember { FocusRequester() }

    val filteredValue = value.filter { it.isDigit() }.take(digitCount)

    LaunchedEffect(filteredValue) {
        if (filteredValue.length == digitCount && onOtpComplete != null) {
            onOtpComplete(filteredValue)
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // BasicTextField tersembunyi untuk menangani input, paste, dan keyboard
        BasicTextField(
            value = filteredValue,
            onValueChange = { input ->
                val digitsOnly = input.filter { it.isDigit() }.take(digitCount)
                onValueChange(digitsOnly)
            },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier
                .focusRequester(focusRequester)
                .matchParentSize(),
            decorationBox = { /* render digantikan oleh baris kotak visual di bawah */ },
        )

        // Tampilan baris kotak visual
        Row(
            horizontalArrangement = Arrangement.spacedBy(VexSpace.s2),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusRequester.requestFocus() },
            ),
        ) {
            for (index in 0 until digitCount) {
                val char = filteredValue.getOrNull(index)?.toString() ?: ""
                val isFocusedBox = filteredValue.length == index || (filteredValue.length == digitCount && index == digitCount - 1)
                val shape = RoundedCornerShape(VexRadius.md)

                val borderModifier = when {
                    isError -> Modifier.border(1.5.dp, colors.danger, shape)
                    isFocusedBox -> Modifier.border(2.dp, colors.primary, shape)
                    char.isNotEmpty() -> Modifier.border(1.dp, colors.primary.copy(alpha = 0.5f), shape)
                    else -> Modifier.border(1.dp, colors.borderInput, shape)
                }

                Box(
                    modifier = Modifier
                        .width(boxWidth)
                        .height(boxHeight)
                        .clip(shape)
                        .background(colors.surfaceInput)
                        .then(borderModifier),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = colors.text,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "OtpInput 6 Digits - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun OtpInputDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        OtpInput(
            value = "123",
            onValueChange = {},
        )
    }
}

@Preview(name = "OtpInput Error - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun OtpInputLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        OtpInput(
            value = "123456",
            onValueChange = {},
            isError = true,
        )
    }
}
