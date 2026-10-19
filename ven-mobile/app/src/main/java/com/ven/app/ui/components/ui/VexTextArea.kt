package com.ven.app.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Text area multiline bertipe Boxed untuk input deskripsi panjang.
 * Sesuai spesifikasi DESIGN.md §6.1 & §8.13:
 * - Boxed: radius 8dp, fill surfaceInput, outline borderInput.
 * - Tinggi min ≈ 160–190dp.
 * - Label di atas input (titleSmall).
 */
@Composable
fun VexTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "type your description...",
    minHeight: Dp = 175.dp,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = VexTheme.colors
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(VexRadius.md)

    val borderModifier = when {
        isError -> Modifier.border(1.dp, colors.danger, shape)
        isFocused -> Modifier.border(2.dp, colors.primary, shape)
        else -> Modifier.border(1.dp, colors.borderInput, shape)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
                modifier = Modifier.padding(bottom = VexSpace.s2),
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = false,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            cursorBrush = SolidColor(colors.primary),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.text),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .then(borderModifier)
                .clip(shape)
                .background(colors.surfaceInput)
                .onFocusChanged { isFocused = it.isFocused }
                .padding(horizontal = VexSpace.s6, vertical = VexSpace.s4),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textMuted,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = colors.danger,
                modifier = Modifier.padding(top = VexSpace.s1, start = VexSpace.s4),
            )
        }
    }
}

@Preview(name = "VexTextArea - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexTextAreaDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        Box(modifier = Modifier.padding(16.dp)) {
            VexTextArea(
                label = "Description",
                value = "",
                onValueChange = {},
            )
        }
    }
}
