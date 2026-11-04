package com.ven.app.data.api.models

import com.google.gson.annotations.SerializedName

/**
 * Wrapper pembungkus paginasi standar Laravel.
 */
data class PaginatedData<T>(
    @SerializedName("current_page")
    val currentPage: Int = 1,

    @SerializedName("data")
    val data: List<T> = emptyList(),

    @SerializedName("last_page")
    val lastPage: Int = 1,

    @SerializedName("total")
    val total: Int = 0,
)

/**
 * Model respons data karya untuk feed beranda.
 */
data class KaryaItemModel(
    @SerializedName("id_karya")
    val idKarya: String,

    @SerializedName("id_pengguna")
    val idPengguna: String? = null,

    @SerializedName("id_pameran")
    val idPameran: Int? = null,

    @SerializedName("id_kategori")
    val idKategori: Int? = null,

    @SerializedName("judul")
    val judul: String,

    @SerializedName("deskripsi")
    val deskripsi: String? = null,

    @SerializedName("file")
    val file: String? = null,

    @SerializedName("file_url")
    val fileUrl: String? = null,

    @SerializedName("jenis")
    val jenis: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("kategori")
    val kategori: KategoriModel? = null,

    @SerializedName("pengguna")
    val pengguna: UserModel? = null,
)

/**
 * Model respons data pameran untuk carousel beranda.
 */
data class PameranItemModel(
    @SerializedName("id_pameran")
    val idPameran: Int,

    @SerializedName("id_pengguna")
    val idPengguna: String? = null,

    @SerializedName("id_model")
    val idModel: Int? = null,

    @SerializedName("judul")
    val judul: String,

    @SerializedName("tipe")
    val tipe: String? = null,

    @SerializedName("warna")
    val warna: String? = null,

    @SerializedName("banner")
    val banner: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("pengguna")
    val pengguna: UserModel? = null,
)

