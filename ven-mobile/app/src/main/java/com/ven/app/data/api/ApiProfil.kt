package com.ven.app.data.api

import com.google.gson.annotations.SerializedName
import com.ven.app.data.api.models.ApiResponse
import com.ven.app.data.api.models.KategoriModel
import com.ven.app.data.api.models.UserModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

data class MinatRequest(
    @SerializedName("kategori")
    val kategori: List<MinatItem>,
)

data class MinatItem(
    @SerializedName("id_kategori")
    val idKategori: Int,

    @SerializedName("bobot")
    val bobot: Double = BOBOT_DEFAULT,
) {
    companion object {
        const val BOBOT_DEFAULT = 1.0
    }
}

data class UpdateProfilRequest(
    @SerializedName("nama")
    val nama: String,

    @SerializedName("nama_panggilan")
    val namaPanggilan: String,

    @SerializedName("deskripsi")
    val deskripsi: String,
)

interface ApiProfilService {

    @GET("profil/kategori")
    suspend fun kategori(): Response<ApiResponse<List<KategoriModel>>>

    @GET("profil/minat")
    suspend fun minat(): Response<ApiResponse<List<KategoriModel>>>

    @PUT("profil/minat")
    suspend fun simpanMinat(
        @Body request: MinatRequest,
    ): Response<ApiResponse<List<KategoriModel>>>

    @PUT("profil")
    suspend fun updateProfil(
        @Body request: UpdateProfilRequest,
    ): Response<ApiResponse<UserModel>>
}

object ApiProfil {

    private val service: ApiProfilService by lazy {
        ApiClient.createService(ApiProfilService::class.java)
    }

    suspend fun kategori(): Result<List<KategoriModel>> = hasil {
        service.kategori()
    }

    suspend fun minat(): Result<List<KategoriModel>> = hasil {
        service.minat()
    }

    suspend fun simpanMinat(idKategori: Set<Int>): Result<List<KategoriModel>> = hasil {
        service.simpanMinat(
            MinatRequest(
                kategori = idKategori.map { MinatItem(idKategori = it) },
            ),
        )
    }

    suspend fun updateProfil(
        nama: String,
        namaPanggilan: String,
        deskripsi: String,
    ): Result<UserModel> = hasil {
        service.updateProfil(
            UpdateProfilRequest(
                nama = nama,
                namaPanggilan = namaPanggilan,
                deskripsi = deskripsi,
            ),
        )
    }

    private suspend fun <T> hasil(
        request: suspend () -> Response<ApiResponse<T>>,
    ): Result<T> {
        return try {
            val response = request()
            val data = response.body()?.data

            if (response.isSuccessful && data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception(ApiClient.parseError(response.errorBody()?.string())))
            }
        } catch (exception: Exception) {
            Result.failure(Exception(exception.localizedMessage ?: "Gagal terhubung ke server"))
        }
    }
}
