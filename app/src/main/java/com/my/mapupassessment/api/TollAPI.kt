package com.my.mapupassessment.api

import com.my.mapupassessment.data.TollPriceRequest
import com.my.mapupassessment.data.response.TollPriceResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface TollAPI {


    @POST("complete-polyline-from-mapping-service")
    suspend fun fetchTollPrice(
        @Body request: TollPriceRequest
    ): Response<TollPriceResponse>

}