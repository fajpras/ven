package com.ven.app.data.api.models

import com.google.gson.annotations.SerializedName

/**
 * Model hasil pencarian ringkasan tipe 'semua'.
 */
data class SearchSummaryModel(
    @SerializedName("karya")
    val karya: List<KaryaItemModel> = emptyList(),

    @SerializedName("pameran")
    val pameran: List<PameranItemModel> = emptyList(),

    @SerializedName("pengguna")
    val pengguna: List<UserModel> = emptyList(),
)

