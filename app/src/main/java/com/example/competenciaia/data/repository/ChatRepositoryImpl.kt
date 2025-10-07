package com.example.competenciaia.data.repository

import android.util.Log
import com.example.competenciaia.data.remote.ChatRequest
import com.example.competenciaia.data.remote.MessageDto
import com.example.competenciaia.data.remote.RetrofitClient
import com.example.competenciaia.domain.model.Message

class ChatRepositoryImpl : ChatRepository {

    private val apiService = RetrofitClient.instance

    override suspend fun getAIResponse(userMessage: String): Message {
        val mensajeFinal = if (contieneHabitos(userMessage)) {
            generarPromptHabitosDetallado(userMessage)
        } else {
            userMessage
        }

        val request = ChatRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(MessageDto(role = "user", content = userMessage))
        )

        return try {
            val response = apiService.getChatCompletions(request)
            Log.d("OpenAI_Response", response.toString())

            val aiMessageContent = response.choices.firstOrNull()?.message?.content
                ?: "No se recibió respuesta."

            val respuestaAmigable = formatearRespuesta(aiMessageContent)

            Message(text = respuestaAmigable, isFromUser = false)
        } catch (e: Exception) {
            Log.e("OpenAI_Error", "Error al llamar a la API: ${e.message}", e)
            Message(text = "Error: No se pudo conectar con la IA.", isFromUser = false)
        }
    }

    private fun contieneHabitos(texto: String): Boolean {
        val palabrasClave = listOf(
            "hábito", "habitos", "hábitos", "sueño", "duermo", "cansado", "ejercicio", "deporte",
            "alimentación", "comida", "estrés", "estresado", "bienestar", "rutina", "vida saludable"
        )
        return palabrasClave.any { texto.lowercase().contains(it) }
    }

    private fun generarPromptHabitosDetallado(entradaUsuario: String): String { //Prompt
        val texto = entradaUsuario.lowercase()

        // Extraer posibles datos
        val horasSueno = Regex("""(\d+)\s*(hora|horas)""").find(texto)?.groups?.get(1)?.value
        val ejercicio = when {
            texto.contains("no hago ejercicio") -> "No realiza ejercicio actualmente"
            texto.contains("1") && texto.contains("vez") -> "Ejercicio 1 vez por semana"
            texto.contains("2") || texto.contains("3") -> "Ejercicio moderado (2-3 veces por semana)"
            texto.contains("4") || texto.contains("5") -> "Ejercicio frecuente (4-5 veces por semana)"
            else -> "No se especifica la frecuencia de ejercicio"
        }
        val alimentacion = when {
            texto.contains("mal") || texto.contains("procesad") -> "Alimentación poco saludable"
            texto.contains("bien") || texto.contains("balance") -> "Alimentación equilibrada"
            else -> "No se especifica la calidad de la alimentación"
        }
        val estres = when {
            texto.contains("estresado") || texto.contains("estrés alto") -> "Estrés alto"
            texto.contains("tranquilo") || texto.contains("relajado") -> "Estrés bajo"
            else -> "Nivel de estrés no especificado"
        }

        // Construcción del resumen detectado
        val resumen = """
            Horas de sueño: ${horasSueno ?: "No especificadas"}
            Ejercicio: $ejercicio
            Alimentación: $alimentacion
            Estrés: $estres
        """.trimIndent()

        // Prompt enriquecido
        return """
            Eres un asesor experto en bienestar físico y mental.
            El usuario te ha compartido lo siguiente sobre sus hábitos:
            
            "$entradaUsuario"
            
            A partir de esto, tu sistema detectó la siguiente información:
            $resumen
            
            Con base en estos datos, elabora recomendaciones prácticas y personalizadas
            para mejorar su bienestar general. Divide la respuesta en secciones:
            "Sueño", "Ejercicio", "Alimentación" y "Bienestar mental".
            
            Usa un tono empático, positivo y motivador.
            Sé claro, conciso y termina con una frase inspiradora corta.
        """.trimIndent()
    }

    private fun formatearRespuesta(texto: String): String {
        return texto
            .replace("•", "+")
            .replace("Sueño", "Sueño")
            .replace("Ejercicio", "Ejercicio")
            .replace("Alimentación", "Alimentación")
            .replace("Bienestar mental", "Bienestar mental")
            .replace("\n\n", "\n")
            .trim()
    }
}