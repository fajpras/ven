package com.ven.app.data.api

import com.google.gson.annotations.SerializedName
import com.ven.app.data.api.models.ApiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

data class BannerUploadData(
    @SerializedName("url")
    val url: String,

    @SerializedName("path")
    val path: String? = null,
)

interface ApiPameranService {
    @Multipart
    @POST("pameran/banner")
    suspend fun uploadBanner(
        @Part banner: MultipartBody.Part,
    ): Response<ApiResponse<BannerUploadData>>
}

object ApiPameran {
    private val service: ApiPameranService by lazy {
        ApiClient.createService(ApiPameranService::class.java)
    }

    suspend fun uploadBanner(file: File, mimeType: String = "image/jpeg"): Result<BannerUploadData> {
        return try {
            val body = file.asRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("banner", file.name, body)
            val response = service.uploadBanner(part)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) {
                Result.success(data)
            } else {
                Result.failure(Exception(ApiClient.parseError(response.errorBody()?.string())))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
