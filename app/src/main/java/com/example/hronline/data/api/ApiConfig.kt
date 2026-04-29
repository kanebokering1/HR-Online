package com.example.hronline.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.hronline.BuildConfig

object ApiConfig {

    // ── Environment (configured via BuildConfig per build type) ───────────
    // Debug   → http://10.0.2.2/api/         (Laragon local from emulator)
    // Release → https://hroes.arthacodestudio.com/api/  (production)
    val BASE_URL: String = BuildConfig.API_BASE_URL
    val TENANT_DOMAIN: String = BuildConfig.TENANT_DOMAIN

    // ── Timeouts (seconds) ────────────────────────────────────────────────
    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT    = 30L
    private const val WRITE_TIMEOUT   = 30L

    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    // X-Tenant header for multi-tenancy (helps backend identify tenant
                    // even when reverse-proxied or accessed via IP)
                    .addHeader("X-Tenant", TENANT_DOMAIN)

                // Add Authorization header if token exists
                TokenManager.token?.let {
                    builder.addHeader("Authorization", "Bearer $it")
                }

                chain.proceed(builder.build())
            }
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy { retrofit.create(ApiService::class.java) }
}
