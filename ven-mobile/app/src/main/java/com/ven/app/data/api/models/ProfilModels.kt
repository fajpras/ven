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
