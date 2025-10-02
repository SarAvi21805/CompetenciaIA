package com.example.competenciaia.data.repository

import android.util.Log
import com.example.competenciaia.data.remote.ChatRequest
import com.example.competenciaia.data.remote.MessageDto
import com.example.competenciaia.data.remote.RetrofitClient
import com.example.competenciaia.domain.model.Message

class ChatRepositoryImpl : ChatRepository {

    private val apiService = RetrofitClient.instance

    override suspend fun getAIResponse(userMessage: String): Message {
        val request = ChatRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(MessageDto(role = "user", content = userMessage))
        )

        return try {
            val response = apiService.getChatCompletions(request)

            // ¡Imprimir la respuesta completa al log!
            Log.d("OpenAI_Response", response.toString())

            val aiMessageContent = response.choices.firstOrNull()?.message?.content ?: "No se recibió respuesta."
            Message(text = aiMessageContent, isFromUser = false)
        } catch (e: Exception) {
            Log.e("OpenAI_Error", "Error al llamar a la API: ${e.message}", e)
            Message(text = "Error: No se pudo conectar con la IA.", isFromUser = false)
        }
    }
}