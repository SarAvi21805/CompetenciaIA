package com.example.competenciaia.data.repository

import android.util.Log
import com.example.competenciaia.BuildConfig // <-- ¡LA IMPORTACIÓN AÑADIDA!
import com.example.competenciaia.data.remote.ChatRequest
import com.example.competenciaia.data.remote.MessageDto
import com.example.competenciaia.data.remote.RetrofitClient
import com.example.competenciaia.domain.model.Message

class ChatRepositoryImpl : ChatRepository {

    private val apiService = RetrofitClient.instance

    override suspend fun getAIResponse(userMessage: String): Message {
        Log.d("OpenAI_Debug", "Iniciando getAIResponse con prompt: $userMessage")

        val apiKey = BuildConfig.OPENAI_API_KEY
        if (apiKey.isBlank() || apiKey == "null") {
            Log.e("OpenAI_Error", "La API Key está vacía o es 'null'. Revisa local.properties y la configuración de build.gradle.kts.")
            return Message(text = "Error: La clave de API no está configurada.", isFromUser = false)
        }

        Log.d("OpenAI_Debug", "API Key cargada correctamente. Creando la petición...")

        val request = ChatRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(MessageDto(role = "user", content = userMessage))
        )

        return try {
            Log.d("OpenAI_Debug", "Intentando realizar la llamada de red...")
            val response = apiService.getChatCompletions(request)
            Log.d("OpenAI_Debug", "Llamada de red exitosa. Respuesta recibida.")

            val aiMessageContent = response.choices.firstOrNull()?.message?.content ?: "No se recibió contenido en la respuesta."
            Message(text = aiMessageContent, isFromUser = false)

        } catch (e: Exception) {
            Log.e("OpenAI_Error", "Error al llamar a la API: ${e.message}", e)
            Message(text = "Error: No se pudo conectar con la IA.", isFromUser = false)
        }
    }
}