package com.ven.app.ui.components.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

@Composable
fun VexListRow(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDanger: Boolean = false,
    showChevron: Boolean = !isDanger,
) {
    val colors = VexTheme.colors
    val contentColor = if (isDanger) colors.danger else colors.text

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = contentColor.copy(alpha = 0.15f)),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = VexSpace.s6, vertical = VexSpace.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VexIcon(
            id = icon,
            contentDescription = label,
            size = VexSize.icon,
            tint = contentColor,
        )

        Spacer(modifier = Modifier.width(VexSpace.s4))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = contentColor,
            modifier = Modifier.weight(1f),
        )

        if (showChevron) {
            VexIcon(
                id = VexIcons.ChevronRight,
                contentDescription = null,
                size = 18.dp,
                tint = colors.text,
            )
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "VexListRow - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexListRowDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VexListRow(
            icon = R.drawable.ic_settings_profile,
            label = "Profile",
            onClick = {},
        )
    }
}

@Preview(name = "VexListRow Danger - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexListRowDangerDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VexListRow(
            icon = R.drawable.ic_settings_logout,
            label = "Logout",
            onClick = {},
            isDanger = true,
        )
    }
}

@Preview(name = "VexListRow - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun VexListRowLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        VexListRow(
            icon = R.drawable.ic_settings_theme,
            label = "Theme",
            onClick = {},
        )
    }
}
