package com.ven.app.data.api.models

import com.google.gson.annotations.SerializedName

/**
 * Satu kategori minat dari tabel `kategori`.
 */
data class KategoriModel(
    @SerializedName("id_kategori")
    val idKategori: Int,

    @SerializedName("kategori")
    val nama: String,
)

/**
 * Hanya mengambil jumlah total dari respons paginasi Laravel.
 */
data class PaginatorTotal(
    @SerializedName("total")
    val total: Int = 0,
)
