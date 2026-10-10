package com.ven.app.data.api.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("data")
    val data: T? = null,

    @SerializedName("message")
    val message: String? = null
)

data class AuthData(
    @SerializedName("pengguna")
    val pengguna: UserModel? = null,

    @SerializedName("token")
    val token: String? = null
)

data class UserModel(
    @SerializedName("id_pengguna")
    val idPengguna: String? = null,

    @SerializedName("nama")
    val nama: String? = null,

    @SerializedName("nama_panggilan")
    val namaPanggilan: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("foto_profil")
    val fotoProfil: String? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("status")
    val status: String? = null
)

data class GoogleAuthRequest(
    @SerializedName("id_token")
    val idToken: String? = null,

    @SerializedName("access_token")
    val accessToken: String? = null
)
