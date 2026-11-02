package com.ven.app.ui.components.feature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ven.app.data.api.models.KategoriModel
import com.ven.app.ui.components.ui.VexSelectableChip
import com.ven.app.ui.theme.VexSpace

/**
 * Deretan chip kategori minat yang dipakai bersama oleh onboarding dan profil.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestCategoryChips(
    categories: List<KategoriModel>,
    selectedIds: Set<Int>,
    onToggle: (idKategori: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(VexSpace.s2),
        verticalArrangement = Arrangement.spacedBy(VexSpace.s2),
    ) {
        categories.forEach { kategori ->
            VexSelectableChip(
                text = kategori.nama,
                selected = kategori.idKategori in selectedIds,
                onSelectedChange = { onToggle(kategori.idKategori) },
                enabled = enabled,
            )
        }
    }
}
