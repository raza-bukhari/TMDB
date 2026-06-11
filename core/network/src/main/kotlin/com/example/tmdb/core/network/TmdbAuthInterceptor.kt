package com.example.tmdb.core.network

import okhttp3.Interceptor
import okhttp3.Response

/** Adds the TMDB v4 read-access bearer token to every request. */
internal class TmdbAuthInterceptor(private val token: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .build(),
        )
}
