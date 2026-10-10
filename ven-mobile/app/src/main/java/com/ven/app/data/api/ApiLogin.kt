package com.ven.app.data.api

import com.google.gson.annotations.SerializedName
import com.ven.app.data.api.models.ApiResponse
import com.ven.app.data.api.models.AuthData
import com.ven.app.data.api.models.GoogleAuthRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    @SerializedName("identitas")
    val identitas: String,

    @SerializedName("kata_sandi")
    val kataSandi: String
)

interface ApiLoginService {

    @POST("auth/masuk")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/google/masuk")
    suspend fun loginGoogle(
        @Body request: GoogleAuthRequest
    ): Response<ApiResponse<AuthData>>

    @POST("auth/keluar")
    suspend fun logout(): Response<ApiResponse<Any>>
}

object ApiLogin {

    private val service: ApiLoginService by lazy {
        ApiClient.createService(ApiLoginService::class.java)
    }

    suspend fun login(identitas: String, kataSandi: String): Result<AuthData> {
        return try {
            val request = LoginRequest(
                identitas = identitas.trim(),
                kataSandi = kataSandi
            )
            val response = service.login(request)
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

    suspend fun loginGoogle(idToken: String?, accessToken: String? = null): Result<AuthData> {
        return try {
            val request = GoogleAuthRequest(
                idToken = idToken,
                accessToken = accessToken
            )
            val response = service.loginGoogle(request)
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

    suspend fun logout(): Result<String> {
        return try {
            val response = service.logout()
            if (response.isSuccessful) {
                val message = response.body()?.message ?: "Logout berhasil."
                Result.success(message)
            } else {
                val errorMsg = ApiClient.parseError(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Gagal terhubung ke server"))
        }
    }
}
