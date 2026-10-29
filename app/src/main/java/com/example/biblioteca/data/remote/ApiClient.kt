package com.example.biblioteca.data.remote

import com.example.biblioteca.data.local.session.SessionManager
import com.example.biblioteca.data.remote.api.BibliotecaApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    const val BASE_URL = "https://biblioteca.moondev.online/api/v1/"
    const val STORAGE_BASE_URL = "https://biblioteca.moondev.online/storage/libros/"

    private var sessionManager: SessionManager? = null
    private var apiServiceInstance: BibliotecaApiService? = null

    fun initialize(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val token = sessionManager?.getAuthToken()

            val requestBuilder = originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header("User-Agent", "BibliotecaUNAMAD-Android/2.0")

            if (!token.isNullOrBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            return chain.proceed(requestBuilder.build())
        }
    }

    fun getApiService(): BibliotecaApiService {
        if (apiServiceInstance == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .addInterceptor(loggingInterceptor)
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiServiceInstance = retrofit.create(BibliotecaApiService::class.java)
        }
        return apiServiceInstance!!
    }
}
