package com.ven.app.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme
import com.ven.app.ui.theme.onCategoryColor

@Composable
fun VexSelectableChip(
    text: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = VexTheme.colors
    val bgColor = if (selected) colors.primary else colors.surfaceInput
    val textColor = if (selected) colors.onPrimary else colors.text

    Box(
        modifier = modifier
            .height(VexSize.chip)
            .clip(RoundedCornerShape(VexRadius.full))
            .background(bgColor)
            .clickable(
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = textColor.copy(alpha = 0.2f)),
                onClick = { onSelectedChange(!selected) },
            )
            .padding(horizontal = VexSpace.s4, vertical = VexSpace.s2),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun VexTagChip(
    text: String,
    categoryColor: Color,
    modifier: Modifier = Modifier,
) {
    val textColor = onCategoryColor(categoryColor)

    Box(
        modifier = modifier
            .height(VexSize.chipSm)
            .clip(RoundedCornerShape(VexRadius.full))
            .background(categoryColor)
            .padding(horizontal = VexSpace.s3, vertical = VexSpace.s1),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun VexCounterChip(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .height(VexSize.chipSm)
            .clip(RoundedCornerShape(VexRadius.full))
            .background(colors.surfaceInput)
            .padding(horizontal = VexSpace.s2, vertical = VexSpace.s1),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+$count",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "VexSelectableChip - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexChipDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VexSelectableChip(text = "Cybersecurity", selected = true, onSelectedChange = {})
    }
}

@Preview(name = "VexSelectableChip - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun VexChipLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        VexSelectableChip(text = "UI/UX", selected = false, onSelectedChange = {})
    }
}
