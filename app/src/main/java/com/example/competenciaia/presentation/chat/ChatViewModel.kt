// Gestión del estado y la lógica de la UI

package com.example.competenciaia.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.competenciaia.data.repository.ChatRepositoryImpl
import com.example.competenciaia.domain.model.Message
import com.example.competenciaia.domain.usecase.GetChatResponseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val getChatResponseUseCase = GetChatResponseUseCase(ChatRepositoryImpl())

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun processIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.SendMessage -> {
                sendMessage(intent.message)
            }
        }
    }

    private fun sendMessage(messageText: String) {
        val userMessage = Message(text = messageText, isFromUser = true)
        _state.value = _state.value.copy(
            messages = _state.value.messages + userMessage,
            isLoading = true
        )

        viewModelScope.launch {
            val aiResponse = getChatResponseUseCase(messageText)
            _state.value = _state.value.copy(
                messages = _state.value.messages + aiResponse,
                isLoading = false
            )
        }
    }
}