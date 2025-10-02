// Acciones del usuario

package com.example.competenciaia.presentation.chat

sealed class ChatIntent {
    data class SendMessage(val message: String) : ChatIntent()
}