package com.ven.app.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Dropdown pilihan kategori/opsi bertipe Boxed.
 * Sesuai spesifikasi DESIGN.md §6.1 & §8.13:
 * - Boxed: radius 8dp, fill surfaceInput, outline borderInput.
 * - Label di atas input (titleSmall).
 * - Ikon chevron-bawah di sisi kanan.
 */
@Composable
fun VexDropdown(
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "Select an option",
    enabled: Boolean = true,
) {
    val colors = VexTheme.colors
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(VexRadius.md)

    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.text,
                modifier = Modifier.padding(bottom = VexSpace.s2),
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(VexSize.control)
                    .clip(shape)
                    .background(colors.surfaceInput)
                    .border(width = 1.dp, color = colors.borderInput, shape = shape)
                    .clickable(
                        enabled = enabled,
                        role = Role.DropdownList,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = colors.text.copy(alpha = 0.12f)),
                        onClick = { expanded = !expanded },
                    )
                    .padding(horizontal = VexSpace.s6),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedOption.ifEmpty { placeholder },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedOption.isNotEmpty()) colors.text else colors.textMuted,
                    modifier = Modifier.weight(1f),
                )

                VexIcon(
                    id = VexIcons.ChevronDown,
                    contentDescription = "Dropdown icon",
                    size = VexSize.icon,
                    tint = colors.text,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(max = 280.dp)
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(VexRadius.md)),
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) colors.primaryText else colors.text,
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = colors.text,
                        ),
                    )
                }
            }
        }
    }
}

@Preview(name = "VexDropdown - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexDropdownDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        Box(modifier = Modifier.padding(16.dp)) {
            VexDropdown(
                label = "Category",
                selectedOption = "Internet Of Things",
                options = listOf("3D", "Animation", "Internet Of Things", "UI/UX"),
                onOptionSelected = {},
            )
        }
    }
}
