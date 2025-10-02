// Lógica de negocio específica

package com.example.competenciaia.domain.usecase

import com.example.competenciaia.data.repository.ChatRepository
import com.example.competenciaia.domain.model.Message

class GetChatResponseUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userMessage: String): Message {
        return chatRepository.getAIResponse(userMessage)
    }
}