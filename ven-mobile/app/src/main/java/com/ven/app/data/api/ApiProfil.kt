package com.ven.app.data.api

import com.google.gson.annotations.SerializedName
import com.ven.app.data.api.models.ApiResponse
import com.ven.app.data.api.models.KaryaItemModel
import com.ven.app.data.api.models.KategoriModel
import com.ven.app.data.api.models.PaginatedData
import com.ven.app.data.api.models.PaginatorTotal
import com.ven.app.data.api.models.PameranItemModel
import com.ven.app.data.api.models.UserModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query
import java.io.File

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

    @SerializedName("nomor_hp")
    val nomorHp: String,

    @SerializedName("deskripsi")
    val deskripsi: String,
)

data class GantiKataSandiRequest(
    @SerializedName("kata_sandi_saat_ini")
    val kataSandiSaatIni: String,

    @SerializedName("kata_sandi")
    val kataSandi: String,
)

data class KirimOtpGantiEmailRequest(
    @SerializedName("email_baru")
    val emailBaru: String,

    @SerializedName("kata_sandi")
    val kataSandi: String,
)

data class GantiEmailRequest(
    @SerializedName("email_baru")
    val emailBaru: String,

    @SerializedName("otp")
    val otp: String,
)

data class UpdateStatusRequest(
    @SerializedName("status")
    val status: String,

    @SerializedName("kata_sandi")
    val kataSandi: String,
)

interface ApiProfilService {

    @GET("profil")
    suspend fun profil(): Response<ApiResponse<UserModel>>

    @GET("profil/kategori")
    suspend fun kategori(): Response<ApiResponse<List<KategoriModel>>>

    @GET("profil/minat")
    suspend fun minat(): Response<ApiResponse<List<KategoriModel>>>

    @GET("karya")
    suspend fun karyaMilikku(
        @Query("milikku") milikku: Int = 1,
    ): Response<ApiResponse<PaginatorTotal>>

    @GET("karya")
    suspend fun listKaryaMilikku(
        @Query("milikku") milikku: Int = 1,
    ): Response<ApiResponse<PaginatedData<KaryaItemModel>>>

    @GET("pameran")
    suspend fun pameranMilikku(
        @Query("milikku") milikku: Int = 1,
    ): Response<ApiResponse<PaginatorTotal>>

    @GET("pameran")
    suspend fun listPameranMilikku(
        @Query("milikku") milikku: Int = 1,
    ): Response<ApiResponse<PaginatedData<PameranItemModel>>>

    @PUT("profil/minat")
    suspend fun simpanMinat(
        @Body request: MinatRequest,
    ): Response<ApiResponse<List<KategoriModel>>>

    @PUT("profil")
    suspend fun updateProfil(
        @Body request: UpdateProfilRequest,
    ): Response<ApiResponse<UserModel>>

    @PUT("profil/kata-sandi")
    suspend fun updateKataSandi(
        @Body request: GantiKataSandiRequest,
    ): Response<ApiResponse<Any>>

    @POST("profil/email/kirim-otp")
    suspend fun kirimOtpGantiEmail(
        @Body request: KirimOtpGantiEmailRequest,
    ): Response<ApiResponse<Any>>

    @PUT("profil/email")
    suspend fun gantiEmail(
        @Body request: GantiEmailRequest,
    ): Response<ApiResponse<UserModel>>

    @PUT("profil/status")
    suspend fun updateStatus(
        @Body request: UpdateStatusRequest,
    ): Response<ApiResponse<UserModel>>

    @Multipart
    @POST("profil/foto")
    suspend fun uploadFoto(
        @Part foto: MultipartBody.Part,
    ): Response<ApiResponse<UserModel>>
}

object ApiProfil {

    private val service: ApiProfilService by lazy {
        ApiClient.createService(ApiProfilService::class.java)
    }

    suspend fun profil(): Result<UserModel> = hasil {
        service.profil()
    }

    suspend fun kategori(): Result<List<KategoriModel>> = hasil {
        service.kategori()
    }

    suspend fun minat(): Result<List<KategoriModel>> = hasil {
        service.minat()
    }

    suspend fun jumlahKarya(): Result<Int> = hasil {
        service.karyaMilikku()
    }.map { it.total }

    suspend fun listKaryaMilikku(): Result<List<KaryaItemModel>> = hasil {
        service.listKaryaMilikku()
    }.map { it.data }

    suspend fun jumlahPameran(): Result<Int> = hasil {
        service.pameranMilikku()
    }.map { it.total }

    suspend fun listPameranMilikku(): Result<List<PameranItemModel>> = hasil {
        service.listPameranMilikku()
    }.map { it.data }

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
        nomorHp: String,
        deskripsi: String,
    ): Result<UserModel> = hasil {
        service.updateProfil(
            UpdateProfilRequest(
                nama = nama,
                namaPanggilan = namaPanggilan,
                nomorHp = nomorHp.trim(),
                deskripsi = deskripsi,
            ),
        )
    }

    suspend fun updateKataSandi(
        kataSandiSaatIni: String,
        kataSandi: String,
    ): Result<String> = hasilPesan {
        service.updateKataSandi(
            GantiKataSandiRequest(
                kataSandiSaatIni = kataSandiSaatIni,
                kataSandi = kataSandi,
            ),
        )
    }

    suspend fun kirimOtpGantiEmail(
        emailBaru: String,
        kataSandi: String,
    ): Result<String> = hasilPesan {
        service.kirimOtpGantiEmail(
            KirimOtpGantiEmailRequest(
                emailBaru = emailBaru.trim(),
                kataSandi = kataSandi,
            ),
        )
    }

    suspend fun gantiEmail(
        emailBaru: String,
        otp: String,
    ): Result<UserModel> = hasil {
        service.gantiEmail(
            GantiEmailRequest(
                emailBaru = emailBaru.trim(),
                otp = otp.trim(),
            ),
        )
    }

    suspend fun updateStatus(
        status: String,
        kataSandi: String,
    ): Result<String> = hasilPesan {
        service.updateStatus(
            UpdateStatusRequest(
                status = status,
                kataSandi = kataSandi,
            ),
        )
    }

    suspend fun uploadFoto(
        file: File,
        mimeType: String,
    ): Result<UserModel> = hasil {
        val body = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("foto", file.name, body)
        service.uploadFoto(part)
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

    private suspend fun <T> hasilPesan(
        request: suspend () -> Response<ApiResponse<T>>,
    ): Result<String> {
        return try {
            val response = request()

            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Berhasil")
            } else {
                Result.failure(Exception(ApiClient.parseError(response.errorBody()?.string())))
            }
        } catch (exception: Exception) {
            Result.failure(Exception(exception.localizedMessage ?: "Gagal terhubung ke server"))
        }
    }
}
