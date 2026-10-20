package com.ven.app.ui.screens.create.exhibit.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitFurnitureCategory
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitFurnitureItem
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitFurnitureType
import com.ven.app.ui.screens.create.exhibit.editor.sampleFurnitureItems
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Panel Bawah Penempatan Furnitur / Objek Dekorasi 3D (Wireframe Create Exhibition - Frame 4).
 *
 * Menampilkan:
 * 1. 4 Tab Kategori: "Light", "Chair", "Plant", "Other" dengan indikator garis bawah aktif.
 * 2. Baris 3 Kartu Objek berbentuk rounded square yang dapat diklik untuk menambahkan objek ke ruangan.
 * 3. Indikator Paginasi Titik (• • •).
 */
@Composable
fun ExhibitFurnitureBottomPanel(
    furnitureCounts: Map<ExhibitFurnitureType, Int>,
    onIncrement: (ExhibitFurnitureType) -> Unit,
    onDecrement: (ExhibitFurnitureType) -> Unit,
    modifier: Modifier = Modifier,
    furnitureItemCounts: Map<String, Int> = emptyMap(),
    onToggleFurnitureItem: ((String) -> Unit)? = null,
    items: List<ExhibitFurnitureItem> = sampleFurnitureItems,
) {
    val colors = VexTheme.colors
    val panelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    var selectedCategory by remember { mutableStateOf(ExhibitFurnitureCategory.Light) }

    val filteredItems = remember(selectedCategory, items) {
        items.filter { it.category == selectedCategory }.take(3)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = panelShape)
            .clip(panelShape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border.copy(alpha = 0.5f), shape = panelShape)
            .navigationBarsPadding()
            .padding(horizontal = VexSpace.s6, vertical = VexSpace.s5),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 4 Tab Kategori Furnitur
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExhibitFurnitureCategory.entries.forEach { category ->
                    val title = when (category) {
                        ExhibitFurnitureCategory.Light -> stringResource(R.string.create_exhibit_tab_light)
                        ExhibitFurnitureCategory.Chair -> stringResource(R.string.create_exhibit_tab_chair)
                        ExhibitFurnitureCategory.Plant -> stringResource(R.string.create_exhibit_tab_plant)
                        ExhibitFurnitureCategory.Other -> stringResource(R.string.create_exhibit_tab_other)
                    }

                    FurnitureCategoryTabItem(
                        title = title,
                        isSelected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 3 Kartu Objek Dekorasi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (i in 0 until 3) {
                    val item = filteredItems.getOrNull(i)
                    val count = item?.let {
                        if (furnitureItemCounts.isNotEmpty()) {
                            furnitureItemCounts[it.id] ?: 0
                        } else {
                            furnitureCounts[it.type] ?: 0
                        }
                    } ?: 0

                    FurnitureObjectCard(
                        item = item,
                        count = count,
                        onClick = {
                            if (item != null) {
                                if (onToggleFurnitureItem != null) {
                                    onToggleFurnitureItem(item.id)
                                } else {
                                    onIncrement(item.type)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Indikator Paginasi Titik (• • •)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.text),
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(colors.border),
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(colors.border),
                )
            }
        }
    }
}

@Composable
private fun FurnitureCategoryTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp,
            ),
            color = if (isSelected) colors.text else colors.textMuted,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(2.dp)
                .background(if (isSelected) colors.text else Color.Transparent),
        )
    }
}

@Composable
private fun FurnitureObjectCard(
    item: ExhibitFurnitureItem?,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val isPlaced = count > 0

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(VexRadius.md))
            .background(colors.surfaceInput)
            .border(
                width = if (isPlaced) 2.dp else 1.dp,
                color = if (isPlaced) colors.primary else colors.border,
                shape = RoundedCornerShape(VexRadius.md),
            )
            .clickable(
                enabled = item != null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (item != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(6.dp),
            ) {
                VexIcon(
                    id = item.iconRes,
                    contentDescription = item.name,
                    size = 32.dp,
                    tint = if (isPlaced) colors.primary else colors.textMuted,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.text,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }

            // Badge Penghitung Penempatan jika > 0
            if (isPlaced) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        ),
                        color = Color.White,
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(name = "Furniture Bottom Panel - Light")
@Composable
private fun ExhibitFurnitureBottomPanelLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ExhibitFurnitureBottomPanel(
            furnitureCounts = mapOf(ExhibitFurnitureType.Spotlight to 2),
            onIncrement = {},
            onDecrement = {},
        )
    }
}
