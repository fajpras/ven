package com.ven.app.ui.components.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Baris item seleksi dengan tanda centang (Checkmark) untuk Language.
 * - Sisi kiri: Label bahasa.
 * - Sisi kanan: Ikon tanda centang [VexIcons.Check] jika terpilih.
 */
@Composable
fun VexCheckRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.text.copy(alpha = 0.15f)),
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = VexSpace.s6, vertical = VexSpace.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = colors.text,
            modifier = Modifier.weight(1f),
        )

        if (selected) {
            VexIcon(
                id = VexIcons.Check,
                contentDescription = "Selected",
                size = 20.dp,
                tint = colors.text,
            )
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "VexCheckRow - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexCheckRowDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        androidx.compose.foundation.layout.Column {
            VexCheckRow(label = "English", selected = true, onClick = {})
            VexCheckRow(label = "Bahasa Indonesia", selected = false, onClick = {})
        }
    }
}

@Preview(name = "VexCheckRow - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun VexCheckRowLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        androidx.compose.foundation.layout.Column {
            VexCheckRow(label = "English", selected = false, onClick = {})
            VexCheckRow(label = "Bahasa Indonesia", selected = true, onClick = {})
        }
    }
}
