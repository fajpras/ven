package com.ven.app.data.api

import com.ven.app.data.api.models.ApiResponse
import com.ven.app.data.api.models.KaryaItemModel
import com.ven.app.data.api.models.PaginatedData
import com.ven.app.data.api.models.PameranItemModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiHome {

    @GET("karya/rekomendasi")
    suspend fun getRekomendasiKarya(
        @Query("page") page: Int = 1,
        @Query("exclude") exclude: String? = null,
        @Query("refresh") refresh: Boolean? = null,
    ): Response<ApiResponse<PaginatedData<KaryaItemModel>>>

    @GET("pameran/rekomendasi")
    suspend fun getRekomendasiPameran(
        @Query("page") page: Int = 1,
    ): Response<ApiResponse<PaginatedData<PameranItemModel>>>
}

