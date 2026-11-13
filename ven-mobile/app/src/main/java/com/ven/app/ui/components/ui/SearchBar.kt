package com.ven.app.ui.components.ui

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Komponen SearchBar berbentuk pill untuk VEN (Virtual Exhibition).
 * Sesuai DESIGN.md §6.1 & §8.4.1:
 * - Bentuk: Pill ([VexRadius.full]).
 * - Tinggi: [VexSize.control] (44–46dp).
 * - Background: [VexTheme.colors.surfaceInput].
 * - Leading icon: [VexIcons.Search] warna [VexTheme.colors.textMuted].
 * - Teks input: [VexTheme.colors.text].
 * - Placeholder: [VexTheme.colors.textMuted].
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.schedule_search_placeholder),
    onSearch: (() -> Unit)? = null,
    enabled: Boolean = true,
    onBackClick: (() -> Unit)? = null,
) {
    val colors = VexTheme.colors
    val shape = RoundedCornerShape(VexRadius.full)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(VexSize.control)
            .clip(shape)
            .background(colors.surfaceInput)
            .padding(horizontal = VexSpace.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading icon: tombol kembali (mode Search Active) atau ikon pencarian.
        if (onBackClick != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 16.dp),
                        role = Role.Button,
                        onClick = onBackClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                VexIcon(
                    id = VexIcons.Back,
                    contentDescription = "Back",
                    size = VexSize.iconSm,
                    tint = colors.text,
                )
            }
        } else {
            VexIcon(
                id = VexIcons.Search,
                contentDescription = placeholder,
                size = 20.dp,
                tint = colors.textMuted,
            )
        }

        Spacer(modifier = Modifier.width(VexSpace.s3))

        // Input Field
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                )
            }

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                enabled = enabled,
                singleLine = true,
                cursorBrush = SolidColor(colors.primary),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.text),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Clear query action if not empty
        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 16.dp),
                        role = Role.Button,
                        onClick = { onQueryChange("") },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
            }
        }
    }
}

@Preview(name = "SearchBar - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun SearchBarDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        Box(modifier = Modifier.padding(16.dp)) {
            SearchBar(
                query = "",
                onQueryChange = {},
            )
        }
    }
}

@Preview(name = "SearchBar with Text - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun SearchBarWithTextDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        Box(modifier = Modifier.padding(16.dp)) {
            SearchBar(
                query = "Open Source",
                onQueryChange = {},
            )
        }
    }
}
