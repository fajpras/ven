package com.ven.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.data.api.models.KategoriModel
import com.ven.app.ui.components.feature.InterestCategoryChips
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.Avatar
import com.ven.app.ui.components.ui.AvatarSize
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.components.ui.VexTextFieldVariant
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

private val USERNAME_REGEX = Regex("^[A-Za-z0-9._]{3,30}$")
private val PHONE_REGEX = Regex("^\\+?[0-9]{8,15}$")

/**
 * Halaman Pengaturan & Edit Profil pengguna.
 * Sesuai wireframe `Wireframe - Setting Profile.png` dan `Profile-1.png`:
 * - Mode View: Top bar "Profile" + ikon pensil di kanan atas.
 * - Mode Edit: Top bar "Edit profile" + tombol teks "Save changes" di kanan atas.
 * - Avatar 96dp tengah + teks "New Photo".
 * - Input Boxed: Username, Name, Phone, Description.
 * - Pilihan kategori Interest dengan [InterestCategoryChips].
 */
@Composable
fun ProfileSettingsScreen(
    initialData: ProfileSettingsData,
    categories: List<KategoriModel>,
    onBackClick: () -> Unit,
    onSaveProfile: (ProfileSettingsData) -> Unit,
    modifier: Modifier = Modifier,
    initialIsEditing: Boolean = false,
    isSaving: Boolean = false,
    isUploadingPhoto: Boolean = false,
    errorMessage: String? = null,
    onRetryClick: () -> Unit = {},
    fotoProfilUrl: String? = null,
    fotoBitmap: ImageBitmap? = null,
    onPhotoClick: () -> Unit = {},
) {
    val colors = VexTheme.colors
    var isEditing by remember { mutableStateOf(initialIsEditing) }
    var showErrors by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf(initialData.username) }
    var name by remember { mutableStateOf(initialData.name) }
    var phone by remember { mutableStateOf(initialData.phone) }
    var description by remember { mutableStateOf(initialData.description) }
    var selectedInterestIds by remember { mutableStateOf(initialData.interestIds) }

    val usernameValid = USERNAME_REGEX.matches(username.trim())
    val nameValid = name.trim().isNotEmpty() && name.length <= 30
    val phoneValid = phone.isBlank() || PHONE_REGEX.matches(phone.trim())
    val descriptionValid = description.length <= 150
    val canSave = !isSaving && !isUploadingPhoto && usernameValid && nameValid &&
        phoneValid && descriptionValid

    fun simpan() {
        showErrors = true
        if (!canSave) return

        onSaveProfile(
            ProfileSettingsData(
                username = username.trim(),
                name = name.trim(),
                phone = phone.trim(),
                description = description.trim(),
                interestIds = selectedInterestIds,
            ),
        )
    }

    val topBarTitle = if (isEditing) {
        stringResource(R.string.settings_edit_profile_title)
    } else {
        stringResource(R.string.settings_item_profile)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = topBarTitle,
                onBackClick = {
                    if (isEditing && !initialIsEditing) {
                        isEditing = false
                    } else {
                        onBackClick()
                    }
                },
                trailingContent = {
                    if (isEditing) {
                        TextButton(
                            enabled = canSave,
                            onClick = { simpan() },
                        ) {
                            Text(
                                text = stringResource(R.string.settings_save_changes),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = colors.primaryText,
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { isEditing = true },
                        ) {
                            VexIcon(
                                id = VexIcons.Edit,
                                contentDescription = "Edit Profile",
                                size = 22.dp,
                                tint = colors.text,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VexSpace.s6, vertical = VexSpace.s5)
                .padding(bottom = VexSpace.s8),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar 96dp + Teks "New Photo"
            Avatar(
                initials = username.ifBlank { "G" },
                size = AvatarSize.Lg,
                imageBitmap = fotoBitmap,
            )

            Spacer(modifier = Modifier.height(VexSpace.s2))

            Text(
                text = if (isUploadingPhoto) {
                    stringResource(R.string.settings_photo_uploading)
                } else {
                    stringResource(R.string.settings_new_photo)
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = if (isUploadingPhoto) colors.textMuted else colors.primaryText,
                modifier = Modifier
                    .clickable(enabled = isEditing && !isUploadingPhoto) { onPhotoClick() }
                    .padding(VexSpace.s1),
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(VexSpace.s4))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.danger,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (categories.isEmpty()) {
                    TextButton(onClick = onRetryClick) {
                        Text(
                            text = stringResource(R.string.interest_retry),
                            color = colors.primaryText,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(VexSpace.s6))

            // Form Fields
            VexTextField(
                value = username,
                onValueChange = {
                    if (isEditing) {
                        username = it.take(30)
                        showErrors = false
                    }
                },
                variant = VexTextFieldVariant.Boxed,
                label = stringResource(R.string.settings_username_label),
                enabled = isEditing,
                isError = isEditing && (showErrors || username.isNotEmpty()) && !usernameValid,
                errorMessage = stringResource(R.string.settings_username_error),
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            VexTextField(
                value = name,
                onValueChange = {
                    if (isEditing) {
                        name = it.take(30)
                        showErrors = false
                    }
                },
                variant = VexTextFieldVariant.Boxed,
                label = stringResource(R.string.settings_name_label),
                enabled = isEditing,
                isError = showErrors && !nameValid,
                errorMessage = stringResource(R.string.settings_name_error),
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            VexTextField(
                value = phone,
                onValueChange = {
                    if (isEditing) {
                        phone = it.filter { karakter -> karakter.isDigit() || karakter == '+' }
                            .take(16)
                        showErrors = false
                    }
                },
                variant = VexTextFieldVariant.Boxed,
                label = stringResource(R.string.settings_phone_label),
                enabled = isEditing,
                isError = isEditing && (showErrors || phone.isNotEmpty()) && !phoneValid,
                errorMessage = stringResource(R.string.settings_phone_error),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            VexTextField(
                value = description,
                onValueChange = {
                    if (isEditing) {
                        description = it.take(150)
                        showErrors = false
                    }
                },
                variant = VexTextFieldVariant.Boxed,
                label = stringResource(R.string.settings_description_label),
                enabled = isEditing,
                singleLine = false,
            )

            Text(
                text = stringResource(R.string.settings_description_counter, description.length),
                style = MaterialTheme.typography.bodySmall,
                color = if (descriptionValid) colors.textMuted else colors.danger,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VexSpace.s1, end = VexSpace.s2),
            )

            Spacer(modifier = Modifier.height(VexSpace.s6))

            // Seksi Interest
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.settings_interest_label),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.text,
                    modifier = Modifier.padding(bottom = VexSpace.s3),
                )

                InterestCategoryChips(
                    categories = categories,
                    selectedIds = selectedInterestIds,
                    onToggle = { idKategori ->
                        if (isEditing && !isSaving) {
                            selectedInterestIds = if (idKategori in selectedInterestIds) {
                                selectedInterestIds - idKategori
                            } else {
                                selectedInterestIds + idKategori
                            }
                        }
                    },
                    enabled = isEditing && !isSaving,
                )

                if (categories.isEmpty() && !isSaving) {
                    Text(
                        text = stringResource(R.string.interest_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        modifier = Modifier.padding(top = VexSpace.s2),
                    )
                }

            }
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

private val sampleProfileCategories = listOf(
    KategoriModel(1, "Cybersecurity"),
    KategoriModel(2, "Software"),
    KategoriModel(3, "UI/UX"),
    KategoriModel(6, "3D"),
    KategoriModel(7, "Animation"),
    KategoriModel(10, "Game Dev"),
)

@Preview(name = "ProfileSettings View - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ProfileSettingsViewDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ProfileSettingsScreen(
            initialData = dummySettingsProfile,
            categories = sampleProfileCategories,
            onBackClick = {},
            onSaveProfile = {},
            initialIsEditing = false,
        )
    }
}

@Preview(name = "ProfileSettings Edit - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ProfileSettingsEditLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ProfileSettingsScreen(
            initialData = dummySettingsProfile,
            categories = sampleProfileCategories,
            onBackClick = {},
            onSaveProfile = {},
            initialIsEditing = true,
        )
    }
}
