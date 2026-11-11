package com.ven.app.ui.screens.create.exhibit

import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.ven.app.R
import com.ven.app.ui.components.ui.CreateContentType

enum class ExhibitTemplateType(val dbValue: String) {
    Small("kecil"),
    Medium("sedang"),
    Big("besar"),
    Event("acara");
}

@Immutable
data class ExhibitTemplateItem(
    val id: String,
    val type: ExhibitTemplateType,
    @param:StringRes val nameRes: Int,
    val capacity: Int,
    val scaleMultiplier: Float = 1.0f,
    val unityTemplateId: String = "",
) {
    val tipe: String get() = type.dbValue
}

val defaultExhibitTemplates: List<ExhibitTemplateItem> = listOf(
    ExhibitTemplateItem(
        id = "tpl_small",
        type = ExhibitTemplateType.Small,
        nameRes = R.string.create_exhibit_template_small,
        capacity = 9,
        scaleMultiplier = 0.95f,
        unityTemplateId = "template01",
    ),
    ExhibitTemplateItem(
        id = "tpl_medium",
        type = ExhibitTemplateType.Medium,
        nameRes = R.string.create_exhibit_template_medium,
        capacity = 14,
        scaleMultiplier = 1.0f,
        unityTemplateId = "template02",
    ),
    ExhibitTemplateItem(
        id = "tpl_big",
        type = ExhibitTemplateType.Big,
        nameRes = R.string.create_exhibit_template_big,
        capacity = 21,
        scaleMultiplier = 1.08f,
        unityTemplateId = "template03",
    ),
    ExhibitTemplateItem(
        id = "tpl_event",
        type = ExhibitTemplateType.Event,
        nameRes = R.string.create_exhibit_template_event,
        capacity = 60,
        scaleMultiplier = 1.15f,
        unityTemplateId = "template04",
    ),
)

@Immutable
data class CreateExhibitUiState(
    val selectedContentType: CreateContentType = CreateContentType.Exhibit,
    val templates: List<ExhibitTemplateItem> = defaultExhibitTemplates,
    val selectedTemplate: ExhibitTemplateItem? = null,
    val title: String = "",
    val description: String = "",
    val bannerUri: Uri? = null,
    val bannerUrl: String = "",
    val titleError: String? = null,
    val bannerError: String? = null,
    val isUploadingBanner: Boolean = false,
    val isLoading: Boolean = false,
)
