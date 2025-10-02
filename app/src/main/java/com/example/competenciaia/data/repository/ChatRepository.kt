// Interfaz para el repositorio

// Creación de una interfaz y una implementación simulada del repositorio.

package com.example.competenciaia.data.repository

import com.example.competenciaia.domain.model.Message

interface ChatRepository {
    suspend fun getAIResponse(userMessage: String): Message
}