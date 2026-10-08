package com.ven.app.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.layout.AuthLayout
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.components.ui.VexSelectableChip
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * 14 Kategori minat standar sesuai wireframe onboard resmi (DESIGN.md §8.1).
 */
val DefaultInterestCategories = listOf(
    "Cybersecurity",
    "Software",
    "UI/UX",
    "Videography",
    "Photography",
    "3D",
    "Animation",
    "Internet Of Things",
    "Automation System",
    "Game Dev",
    "Fabrication",
    "Manufacturing",
    "2D",
    "Others",
)

/**
 * Stateful version of [InterestScreen].
 * Mengelola state pemilihan kategori internal dengan [rememberSaveable].
 */
@Composable
fun InterestScreen(
    onSaveSuccess: (selectedCategories: Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    onMaybeLater: () -> Unit = { onSaveSuccess(emptySet()) },
    initialSelected: Set<String> = emptySet(),
    categories: List<String> = DefaultInterestCategories,
    isLoading: Boolean = false,
) {
    var selectedCategories by rememberSaveable {
        mutableStateOf(initialSelected)
    }

    InterestScreenContent(
        categories = categories,
        selectedCategories = selectedCategories,
        onCategoryToggle = { category ->
            selectedCategories = if (category in selectedCategories) {
                selectedCategories - category
            } else {
                selectedCategories + category
            }
        },
        onSaveClick = { onSaveSuccess(selectedCategories) },
        onMaybeLaterClick = onMaybeLater,
        isLoading = isLoading,
        modifier = modifier,
    )
}

/**
 * Stateless content version of [InterestScreen] (DESIGN.md §8.1).
 *
 * Mendukung 2 mode (Dark Mode & Light Mode):
 * - Kerangka: [AuthLayout] dengan contentMaxWidth responsif
 * - Judul: "Interest" (displayMedium)
 * - Pilihan kategori: [FlowRow] dengan [VexSelectableChip]
 * - Aksi bawah: Link "Maybe later" -> HorizontalDivider -> Tombol "Save"
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InterestScreenContent(
    categories: List<String>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onSaveClick: () -> Unit,
    onMaybeLaterClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    val colors = VexTheme.colors

    AuthLayout(
        title = stringResource(R.string.interest_title),
        contentMaxWidth = VexSize.interestContentWidth,
        modifier = modifier,
        formContent = {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VexSpace.s1),
                horizontalArrangement = Arrangement.spacedBy(VexSpace.s2),
                verticalArrangement = Arrangement.spacedBy(VexSpace.s2),
            ) {
                categories.forEach { category ->
                    val isSelected = category in selectedCategories
                    VexSelectableChip(
                        text = category,
                        selected = isSelected,
                        onSelectedChange = { onCategoryToggle(category) },
                    )
                }
            }
        },
        footerContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Link teks "Maybe later"
                VexButton(
                    text = stringResource(R.string.interest_maybe_later),
                    onClick = onMaybeLaterClick,
                    variant = VexButtonVariant.TextLink,
                )

                Spacer(modifier = Modifier.height(VexSpace.s3))

                // Garis pemisah horizontal selebar tombol form
                HorizontalDivider(
                    modifier = Modifier.width(VexSize.authContentWidth),
                    thickness = 1.dp,
                    color = colors.border,
                )

                Spacer(modifier = Modifier.height(VexSpace.s6))

                // Tombol utama "Save"
                VexButton(
                    text = stringResource(R.string.interest_save),
                    onClick = onSaveClick,
                    variant = VexButtonVariant.Primary,
                    loading = isLoading,
                    modifier = Modifier.width(VexSize.authContentWidth),
                )
            }
        },
    )
}

// ---------------------------------------------------------
// Previews untuk 2 Mode: Dark Mode & Light Mode
// ---------------------------------------------------------

@Preview(name = "InterestScreen - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun InterestScreenDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        InterestScreenContent(
            categories = DefaultInterestCategories,
            selectedCategories = setOf("UI/UX", "3D", "Game Dev"),
            onCategoryToggle = {},
            onSaveClick = {},
            onMaybeLaterClick = {},
        )
    }
}

@Preview(name = "InterestScreen - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun InterestScreenLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        InterestScreenContent(
            categories = DefaultInterestCategories,
            selectedCategories = setOf("Cybersecurity", "Animation"),
            onCategoryToggle = {},
            onSaveClick = {},
            onMaybeLaterClick = {},
        )
    }
}
