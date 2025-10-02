package com.example.competenciaia.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletions(
        @Body chatRequest: ChatRequest,
        @Header("Authorization") apiKey: String = "Bearer " +
                "${com.example.competenciaia.BuildConfig.OPENAI_API_KEY}"
    ): ChatResponse
}