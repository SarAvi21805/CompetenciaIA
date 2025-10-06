// Estado de la UI

package com.example.competenciaia.presentation.chat

import com.example.competenciaia.domain.model.Message

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val availableOptions: List<String> = emptyList()
)