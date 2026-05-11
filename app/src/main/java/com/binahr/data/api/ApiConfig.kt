package com.binahr.data.api


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID
import java.util.concurrent.TimeUnit
import android.os.Handler
import android.os.Looper
import com.binahr.BuildConfig

object ApiConfig {

    val BASE_URL: String = BuildConfig.API_BASE_URL

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
                val original = chain.request()
                val builder = original.newBuilder()
                    .header("Accept", "application/json")
                    // Resolve tenant domain dynamically so multi-tenant mobile login works.
                    // TokenManager.effectiveTenantDomain() returns the user-supplied domain
                    // (e.g. "sml.bina-hris.com") or falls back to BuildConfig.TENANT_DOMAIN.
                    .header("X-Tenant", TokenManager.effectiveTenantDomain())

                // Attach Bearer token when available.
                TokenManager.token?.let { builder.header("Authorization", "Bearer $it") }

                // Attach Idempotency-Key for all state-mutating requests to prevent
                // duplicate submissions on network retries.
                val method = original.method.uppercase()
                if (method == "POST" || method == "PUT" || method == "PATCH") {
                    builder.header("Idempotency-Key", UUID.randomUUID().toString())
                }

                chain.proceed(builder.build())
            }
            .authenticator { _, response ->
                // 401 → session expired: clear token and force re-login.
                if (response.code == 401) {
                    TokenManager.clearAuth()
                    Handler(Looper.getMainLooper()).post {
                        TokenManager.onUnauthorized?.invoke()
                    }
                }
                null  // do not retry with a new token
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






















