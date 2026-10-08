package com.ven.app.ui.components.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

enum class VexButtonVariant {
    Primary,
    Inverse,
    Outline,
    Danger,
    TextLink,
}

@Composable
fun VexButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: VexButtonVariant = VexButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    @DrawableRes icon: Int? = null,
) {
    val colors = VexTheme.colors
    val isInteractive = enabled && !loading

    val (bgColor: Color, textColor: Color) = when (variant) {
        VexButtonVariant.Primary -> {
            if (!enabled) colors.primaryDisabled to colors.textDisabled
            else colors.primary to colors.onPrimary
        }
        VexButtonVariant.Inverse -> {
            if (!enabled) colors.surfaceInput to colors.textDisabled
            else colors.inverseBg to colors.inverseText
        }
        VexButtonVariant.Outline -> {
            if (!enabled) colors.surfaceInput to colors.textDisabled
            else Color.White to Color.Black
        }
        VexButtonVariant.Danger -> {
            if (!enabled) colors.danger.copy(alpha = 0.3f) to colors.textDisabled
            else colors.danger to colors.onDanger
        }
        VexButtonVariant.TextLink -> {
            Color.Transparent to if (enabled) colors.primaryText else colors.textDisabled
        }
    }

    if (variant == VexButtonVariant.TextLink) {
        Box(
            modifier = modifier
                .clickable(
                    enabled = isInteractive,
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(vertical = VexSpace.s2, horizontal = VexSpace.s3),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(VexSize.control)
            .clip(RoundedCornerShape(VexRadius.full))
            .background(bgColor)
            .clickable(
                enabled = isInteractive,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = textColor.copy(alpha = 0.2f)),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(VexSize.iconSm),
                color = textColor,
                strokeWidth = VexSpace.s1 / 2,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = VexSpace.s6),
            ) {
                if (icon != null && icon != 0) {
                    VexIcon(
                        id = icon,
                        contentDescription = null,
                        tint = textColor,
                        size = VexSize.iconSm,
                    )
                    Spacer(modifier = Modifier.width(VexSpace.s2))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(name = "VexButton - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexButtonDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VexButton(text = "Login", onClick = {}, variant = VexButtonVariant.Primary)
    }
}

@Preview(name = "VexButton - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun VexButtonLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        VexButton(text = "Register", onClick = {}, variant = VexButtonVariant.Inverse)
    }
}
