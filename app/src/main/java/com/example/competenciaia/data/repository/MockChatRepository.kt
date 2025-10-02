// Implementación simulada

package com.example.competenciaia.data.repository

import com.example.competenciaia.domain.model.Message
import kotlinx.coroutines.delay

class MockChatRepository : ChatRepository {

    private val responses = mapOf(
        "hola" to "¡Hola! ¿Cómo te sientes hoy? Podemos hablar de música o de lo que necesites.",
        "triste" to "Lamento que te sientas así. A veces una buena canción puede ayudar. ¿Qué tipo de música te gusta?",
        "ansioso" to "Entiendo. La ansiedad es difícil. Respirar profundamente puede ayudar. ¿Hay alguna canción que te relaje?",
        "feliz" to "¡Qué bueno! La felicidad es genial. ¿Qué canción describe tu estado de ánimo ahora mismo?",
        "recomiéndame una canción" to "Claro, te recomiendo 'Here Comes The Sun' de The Beatles. Es muy optimista.",
        "dame un consejo" to "Recuerda ser amable contigo mismo. Está bien no estar bien todo el tiempo."
    )

    override suspend fun getAIResponse(userMessage: String): Message {
        delay(1500) // Simula una llamada de red
        val responseText = responses.entries.find { userMessage.lowercase().contains(it.key) }?.value
            ?: "No entendí muy bien. ¿Podrías reformular tu pregunta? O podemos hablar de tu canción favorita."

        return Message(text = responseText, isFromUser = false)
    }
}