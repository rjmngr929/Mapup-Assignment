package com.my.mapupassessment.api

import com.my.mapupassessment.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()

        request.addHeader("x-api-key", BuildConfig.API_KEY)
        return chain.proceed(request.build())
    }
}