package com.ven.app.data.api

import com.ven.app.data.api.models.ApiResponse
import com.ven.app.data.api.models.KaryaItemModel
import com.ven.app.data.api.models.PaginatedData
import com.ven.app.data.api.models.PameranItemModel
import com.ven.app.data.api.models.SearchSummaryModel
import com.ven.app.data.api.models.UserModel
import com.ven.app.data.api.models.UserProfileDetailModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiPencarian {

    @GET("pencarian")
    suspend fun searchSemua(
        @Query("q") query: String? = null,
        @Query("tipe") tipe: String = "semua",
    ): Response<ApiResponse<SearchSummaryModel>>

    @GET("pencarian")
    suspend fun searchKarya(
        @Query("q") query: String? = null,
        @Query("tipe") tipe: String = "karya",
        @Query("page") page: Int = 1,
    ): Response<ApiResponse<PaginatedData<KaryaItemModel>>>

    @GET("pencarian")
    suspend fun searchPameran(
        @Query("q") query: String? = null,
        @Query("tipe") tipe: String = "pameran",
        @Query("page") page: Int = 1,
    ): Response<ApiResponse<PaginatedData<PameranItemModel>>>

    @GET("pencarian")
    suspend fun searchPengguna(
        @Query("q") query: String? = null,
        @Query("tipe") tipe: String = "pengguna",
        @Query("page") page: Int = 1,
    ): Response<ApiResponse<PaginatedData<UserModel>>>

    @GET("pengguna/{id}")
    suspend fun getDetailPengguna(
        @Path("id") idPengguna: String,
    ): Response<ApiResponse<UserProfileDetailModel>>
}
