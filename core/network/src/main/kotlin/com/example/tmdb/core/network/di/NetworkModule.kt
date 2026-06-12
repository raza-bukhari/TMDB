package com.example.tmdb.core.network.di

import com.example.tmdb.core.network.BuildConfig
import com.example.tmdb.core.network.OmdbApi
import com.example.tmdb.core.network.TmdbApi
import com.example.tmdb.core.network.TmdbAuthInterceptor
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File

val networkModule = module {
    single {
        val cacheSize = 10L * 1024 * 1024 // 10 MiB
        val cache = Cache(File(androidContext().cacheDir, "http_cache"), cacheSize)

        OkHttpClient.Builder()
            .cache(cache)
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

    single<OmdbApi> {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        // Separate Retrofit: different host, key-as-query-param auth, no bearer header.
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val url = chain.request().url.newBuilder()
                    .addQueryParameter("apikey", BuildConfig.OMDB_API_KEY)
                    .build()
                chain.proceed(chain.request().newBuilder().url(url).build())
            }
            .build()
        Retrofit.Builder()
            .baseUrl(BuildConfig.OMDB_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OmdbApi::class.java)
    }
}
