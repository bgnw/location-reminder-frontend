package com.bgnw.locationreminder.api

import okhttp3.Interceptor
import okhttp3.Response

class JsonHeaderInterceptor : Interceptor { // https://stackoverflow.com/questions/23238397/
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
//            .addHeader("Connection", "close")
            .build()
        return chain.proceed(request)
    }
}
