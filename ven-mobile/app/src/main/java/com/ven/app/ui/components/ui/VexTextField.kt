package com.ven.app.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

enum class VexTextFieldVariant {
    Pill,
    Boxed,
}

@Composable
fun VexTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: VexTextFieldVariant = VexTextFieldVariant.Pill,
    placeholder: String = "",
    label: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String = "",
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    authDarkMode: Boolean = false,
) {
    val colors = VexTheme.colors
    var isFocused by remember { mutableStateOf(false) }

    val (bgColor, textColor, placeholderColor) = when {
        authDarkMode && variant == VexTextFieldVariant.Pill -> {
            Triple(Color.White, Color.Black, Color(0xFF8A8A92))
        }
        variant == VexTextFieldVariant.Pill -> {
            Triple(colors.surfaceInput, colors.text, colors.textMuted)
        }
        else -> {
            Triple(colors.surfaceInput, colors.text, colors.textMuted)
        }
    }

    val shape = when (variant) {
        VexTextFieldVariant.Pill -> RoundedCornerShape(VexRadius.full)
        VexTextFieldVariant.Boxed -> RoundedCornerShape(VexRadius.md)
    }

    val borderModifier = when {
        isError -> Modifier.border(1.dp, colors.danger, shape)
        isFocused && variant == VexTextFieldVariant.Boxed -> Modifier.border(2.dp, colors.primary, shape)
        variant == VexTextFieldVariant.Boxed -> Modifier.border(1.dp, colors.borderInput, shape)
        else -> Modifier
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (variant == VexTextFieldVariant.Boxed && label.isNotEmpty()) {
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
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(if (authDarkMode) Color.Black else colors.primary),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
            modifier = Modifier
                .fillMaxWidth()
                .height(VexSize.control)
                .then(borderModifier)
                .clip(shape)
                .background(bgColor)
                .onFocusChanged { isFocused = it.isFocused },
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VexSpace.s6),
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = placeholderColor,
                            )
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.padding(start = VexSpace.s2))
                        trailingIcon()
                    }
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

@Composable
fun VexPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: VexTextFieldVariant = VexTextFieldVariant.Pill,
    placeholder: String = "Password",
    label: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    authDarkMode: Boolean = false,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    VexTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        variant = variant,
        placeholder = placeholder,
        label = label,
        enabled = enabled,
        isError = isError,
        errorMessage = errorMessage,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        authDarkMode = authDarkMode,
        trailingIcon = {
            VexButton(
                text = if (passwordVisible) "Hide" else "Show",
                onClick = { passwordVisible = !passwordVisible },
                variant = VexButtonVariant.TextLink,
            )
        },
    )
}

@Preview(name = "VexTextField Pill Auth - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexTextFieldPillDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VexTextField(
            value = "",
            onValueChange = {},
            variant = VexTextFieldVariant.Pill,
            placeholder = "Email/Username",
            authDarkMode = true,
        )
    }
}

@Preview(name = "VexTextField Boxed - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun VexTextFieldBoxedLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        VexTextField(
            value = "",
            onValueChange = {},
            variant = VexTextFieldVariant.Boxed,
            label = "Email",
            placeholder = "Enter email",
        )
    }
}
