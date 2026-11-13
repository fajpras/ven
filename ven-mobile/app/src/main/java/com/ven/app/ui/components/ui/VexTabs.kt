package com.ven.app.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexTheme

@Composable
fun <T> VexIconTabs(
    tabs: List<T>,
    selectedTab: T,
    onTabSelected: (T) -> Unit,
    iconFor: (tab: T, isSelected: Boolean) -> Int,
    contentDescriptionFor: @Composable (tab: T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            val iconRes = iconFor(tab, isSelected)
            val desc = contentDescriptionFor(tab)
            val tint = if (isSelected) colors.text else colors.textMuted
            val underlineColor = if (isSelected) colors.text else colors.border

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = colors.text.copy(alpha = 0.15f)),
                        role = Role.Tab,
                        onClick = { onTabSelected(tab) },
                    )
                    .semantics {
                        this.selected = isSelected
                        this.role = Role.Tab
                        this.contentDescription = desc
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(VexSize.control),
                    contentAlignment = Alignment.Center,
                ) {
                    VexIcon(
                        id = iconRes,
                        contentDescription = desc,
                        size = VexSize.icon,
                        tint = tint,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(underlineColor),
                )
            }
        }
    }
}

@Composable
fun <T> VexTextTabs(
    tabs: List<T>,
    selectedTab: T,
    onTabSelected: (T) -> Unit,
    labelFor: @Composable (tab: T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            val label = labelFor(tab)
            val textColor = if (isSelected) colors.text else colors.textMuted
            val underlineColor = if (isSelected) colors.text else colors.border

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = colors.text.copy(alpha = 0.15f)),
                        role = Role.Tab,
                        onClick = { onTabSelected(tab) },
                    )
                    .semantics {
                        this.selected = isSelected
                        this.role = Role.Tab
                        this.contentDescription = label
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(VexSize.control),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = if (isSelected) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                        color = textColor,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(underlineColor),
                )
            }
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

private enum class SampleProfileTab { Posts, Exhibits }
private enum class SampleScheduleTab { Approved, Waiting, Request }

@Preview(name = "VexTextTabs - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexTextTabsDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VexTextTabs(
            tabs = listOf(SampleScheduleTab.Approved, SampleScheduleTab.Waiting, SampleScheduleTab.Request),
            selectedTab = SampleScheduleTab.Approved,
            onTabSelected = {},
            labelFor = { it.name },
        )
    }
}

@Preview(name = "VexIconTabs - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VexIconTabsDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VexIconTabs(
            tabs = listOf(SampleProfileTab.Posts, SampleProfileTab.Exhibits),
            selectedTab = SampleProfileTab.Posts,
            onTabSelected = {},
            iconFor = { tab, isSelected ->
                when (tab) {
                    SampleProfileTab.Posts -> if (isSelected) VexIcons.Tab.Posts.filled else VexIcons.Tab.Posts.outline
                    SampleProfileTab.Exhibits -> if (isSelected) VexIcons.Tab.Exhibits.filled else VexIcons.Tab.Exhibits.outline
                }
            },
            contentDescriptionFor = { it.name },
        )
    }
}

@Preview(name = "VexIconTabs - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun VexIconTabsLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        VexIconTabs(
            tabs = listOf(SampleProfileTab.Posts, SampleProfileTab.Exhibits),
            selectedTab = SampleProfileTab.Exhibits,
            onTabSelected = {},
            iconFor = { tab, isSelected ->
                when (tab) {
                    SampleProfileTab.Posts -> if (isSelected) VexIcons.Tab.Posts.filled else VexIcons.Tab.Posts.outline
                    SampleProfileTab.Exhibits -> if (isSelected) VexIcons.Tab.Exhibits.filled else VexIcons.Tab.Exhibits.outline
                }
            },
            contentDescriptionFor = { it.name },
        )
    }
}
