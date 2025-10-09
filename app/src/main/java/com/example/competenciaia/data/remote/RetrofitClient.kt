package com.example.competenciaia.data.remote

import com.squareup.moshi.Moshi // <-- Importación necesaria
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory // <-- Importación necesaria
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://api.openai.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // 1. Crear una instancia de Moshi con el adaptador para Kotlin
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val instance: OpenAiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // 2. Usar esa instancia de Moshi en el ConverterFactory
            .addConverterFactory(MoshiConverterFactory.create(moshi)) // <-- CAMBIO CLAVE
            .build()
            .create(OpenAiApiService::class.java)
    }
}