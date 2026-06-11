package com.example.tmdb.core.network.di

import com.example.tmdb.core.network.BuildConfig
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.core.network.TmdbAuthInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .addInterceptor(TmdbAuthInterceptor(BuildConfig.TMDB_API_TOKEN))
            .apply {
                // Body logging leaks PII/token-shaped data; debug builds only.
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()
    }

    single {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        Retrofit.Builder()
            .baseUrl(BuildConfig.TMDB_BASE_URL)
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    single<TmdbApi> { get<Retrofit>().create(TmdbApi::class.java) }
}
