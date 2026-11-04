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

/**
 * Model detail profil pengguna publik beserta karya dan pamerannya.
 */
data class UserProfileDetailModel(
    @SerializedName("pengguna")
    val pengguna: UserProfileInfoModel? = null,

    @SerializedName("karya")
    val karya: List<KaryaItemModel> = emptyList(),

    @SerializedName("pameran")
    val pameran: List<PameranItemModel> = emptyList(),
)

data class UserProfileInfoModel(
    @SerializedName("id_pengguna")
    val idPengguna: String? = null,

    @SerializedName("nama")
    val nama: String? = null,

    @SerializedName("nama_panggilan")
    val namaPanggilan: String? = null,

    @SerializedName("foto_profil")
    val fotoProfil: String? = null,

    @SerializedName("deskripsi")
    val deskripsi: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("post_count")
    val postCount: Int = 0,

    @SerializedName("exhibit_count")
    val exhibitCount: Int = 0,

    @SerializedName("kategori")
    val kategori: List<KategoriModel> = emptyList(),
)
