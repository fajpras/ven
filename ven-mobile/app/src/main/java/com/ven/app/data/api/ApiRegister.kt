package com.ven.app.data.api

import com.google.gson.annotations.SerializedName
import com.ven.app.data.api.models.ApiResponse
import com.ven.app.data.api.models.AuthData
import com.ven.app.data.api.models.GoogleAuthRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class KirimOtpRequest(
    @SerializedName("email")
    val email: String
)

data class RegisterRequest(
    @SerializedName("nama")
    val nama: String,

    @SerializedName("nama_panggilan")
    val namaPanggilan: String? = null,

    @SerializedName("email")
    val email: String,

    @SerializedName("kata_sandi")
    val kataSandi: String,

    @SerializedName("otp")
    val otp: String
)

interface ApiRegisterService {

    @POST("auth/daftar/kirim-otp")
    suspend fun kirimOtpRegistrasi(
        @Body request: KirimOtpRequest
    ): Response<ApiResponse<Any>>

    @POST("auth/daftar")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/google/daftar")
    suspend fun registerGoogle(
        @Body request: GoogleAuthRequest
    ): Response<ApiResponse<AuthData>>
}

object ApiRegister {

    private val service: ApiRegisterService by lazy {
        ApiClient.createService(ApiRegisterService::class.java)
    }

    suspend fun kirimOtp(email: String): Result<String> {
        return try {
            val response = service.kirimOtpRegistrasi(KirimOtpRequest(email = email.trim()))
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Kode verifikasi telah dikirim ke email."
                Result.success(message)
            } else {
                val errorMsg = ApiClient.parseError(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung ke server"))
        }
    }

    suspend fun register(
        nama: String,
        username: String?,
        email: String,
        kataSandi: String,
        otp: String
    ): Result<AuthData> {
        return try {
            val request = RegisterRequest(
                nama = nama.trim(),
                namaPanggilan = username?.trim()?.ifBlank { null },
                email = email.trim(),
                kataSandi = kataSandi,
                otp = otp.trim()
            )
            val response = service.register(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = ApiClient.parseError(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung ke server"))
        }
    }

    suspend fun registerGoogle(idToken: String?, accessToken: String? = null): Result<AuthData> {
        return try {
            val request = GoogleAuthRequest(
                idToken = idToken,
                accessToken = accessToken
            )
            val response = service.registerGoogle(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Result.success(response.body()!!.data!!)
            } else {
                val errorMsg = ApiClient.parseError(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung ke server"))
        }
    }
}
