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

// Enum para controlar en qué punto de la conversación estamos.
private enum class ConversationState {
    IDLE, // Estado inicial, esperando que el usuario pida una recomendación
    ASKING_ENERGY,
    ASKING_SLEEP,
    ASKING_ACTIVITY,
    ASKING_PREFERENCE,
    COMPLETE // Todas las respuestas han sido recolectadas
}

class ChatViewModel : ViewModel() {

    private val getChatResponseUseCase = GetChatResponseUseCase(ChatRepositoryImpl())

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    // ---- Variables para almacenar las respuestas del usuario ----
    private var conversationState = ConversationState.IDLE
    private var userEnergy: String? = null
    private var userSleep: String? = null
    private var userActivity: String? = null
    private var userPreference: String? = null
    // -----------------------------------------------------------

    init {
        // Mensaje de bienvenida inicial
        val welcomeMessage = Message(
            text = "¡Hola! Soy Vita, tu asistente de bienestar. Escribe 'recomiendame una actividad' para empezar.",
            isFromUser = false
        )
        _state.value = _state.value.copy(messages = listOf(welcomeMessage))
    }


    fun processIntent(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.SendMessage -> {
                val userMessage = Message(text = intent.message, isFromUser = true)
                _state.value = _state.value.copy(messages = _state.value.messages + userMessage)

                // Si el usuario pide una recomendación, iniciamos el flujo.
                if (intent.message.contains("recomiendame", ignoreCase = true) || intent.message.contains("actividad", ignoreCase = true)) {
                    startRecommendationFlow()
                } else {
                    // Si no, simplemente llamamos a la IA con el texto.
                    getFinalResponse(intent.message)
                }
            }
            is ChatIntent.OptionSelected -> {
                // El usuario ha presionado un botón.
                handleOptionSelected(intent.optionText)
            }
        }
    }

    private fun startRecommendationFlow() {
        // Reiniciamos por si había una conversación anterior.
        resetConversation()
        val startMessage = Message(
            text = "¡Claro! Para darte la mejor sugerencia, necesito saber un poco más sobre cómo te sientes. Empecemos.",
            isFromUser = false
        )
        _state.value = _state.value.copy(messages = _state.value.messages + startMessage)
        askEnergyQuestion()
    }

    private fun askEnergyQuestion() {
        conversationState = ConversationState.ASKING_ENERGY
        val question = Message(text = "¿Cómo describirías tu nivel de energía en este momento?", isFromUser = false)
        _state.value = _state.value.copy(
            messages = _state.value.messages + question,
            availableOptions = listOf("😴 Poca energía", "😐 Normal", "⚡ Lleno de energía", "🧘‍♂️ Estresado")
        )
    }

    private fun askSleepQuestion() {
        conversationState = ConversationState.ASKING_SLEEP
        val question = Message(text = "¿Qué tal dormiste anoche? ¿Sientes que fue suficiente?", isFromUser = false)
        _state.value = _state.value.copy(
            messages = _state.value.messages + question,
            availableOptions = listOf("👍 Sí, fue reparador", "👎 No, descansé poco", "🤔 Más o menos")
        )
    }

    private fun askActivityQuestion() {
        conversationState = ConversationState.ASKING_ACTIVITY
        val question = Message(text = "Gracias. En las últimas horas, ¿has estado principalmente...?", isFromUser = false)
        _state.value = _state.value.copy(
            messages = _state.value.messages + question,
            availableOptions = listOf("🚶‍♂️ Moviéndome / Activo", "💻 Sentado / Inactivo", "🏃‍♂️ Hice ejercicio")
        )
    }

    private fun askPreferenceQuestion() {
        conversationState = ConversationState.ASKING_PREFERENCE
        val question = Message(text = "¡Perfecto! Por último, ¿qué tipo de actividad te apetece más ahora mismo?", isFromUser = false)
        _state.value = _state.value.copy(
            messages = _state.value.messages + question,
            availableOptions = listOf("🧘‍♀️ Algo relajante", "🏃‍♂️ Algo para activarme", "🌳 Al aire libre", "🏠 En casa")
        )
    }


    private fun handleOptionSelected(option: String) {
        // 1. Añade la respuesta del usuario al historial de chat.
        val userMessage = Message(text = option, isFromUser = true)
        _state.value = _state.value.copy(
            messages = _state.value.messages + userMessage,
            availableOptions = emptyList() // Ocultamos los botones mientras procesamos.
        )

        // 2. Guarda la respuesta y avanza al siguiente estado de la conversación.
        when (conversationState) {
            ConversationState.ASKING_ENERGY -> {
                userEnergy = option
                askSleepQuestion()
            }
            ConversationState.ASKING_SLEEP -> {
                userSleep = option
                askActivityQuestion()
            }
            ConversationState.ASKING_ACTIVITY -> {
                userActivity = option
                askPreferenceQuestion()
            }
            ConversationState.ASKING_PREFERENCE -> {
                userPreference = option
                conversationState = ConversationState.COMPLETE
                // ¡Tenemos todas las respuestas! Ahora construimos el prompt.
                generateAndSendFinalPrompt()
            }
            else -> { /* No hacer nada en otros estados */ }
        }
    }

    private fun generateAndSendFinalPrompt() {
        val thinkingMessage = Message(text = "Entendido. Dame un momento para pensar en la recomendación perfecta para ti...", isFromUser = false)
        _state.value = _state.value.copy(
            messages = _state.value.messages + thinkingMessage,
            isLoading = true
        )

        // Aquí construiríamos el prompt con toda la información recolectada.
        // Por simplicidad, por ahora solo unimos las respuestas.
        val prompt = """
            Rol: Eres un asistente de bienestar.
            Usuario: Se siente con energía '$userEnergy', durmió '$userSleep', su actividad reciente fue '$userActivity' y prefiere algo '$userPreference'.
            Tarea: Dame una recomendación de actividad corta y motivadora (máximo 40 palabras).
        """.trimIndent()

        getFinalResponse(prompt)
    }

    private fun getFinalResponse(prompt: String) {
        viewModelScope.launch {
            val aiResponse = getChatResponseUseCase(prompt)
            _state.value = _state.value.copy(
                messages = _state.value.messages + aiResponse,
                isLoading = false
            )
            // Una vez que la IA responde, reiniciamos el estado de la conversación.
            resetConversation()
        }
    }

    private fun resetConversation() {
        conversationState = ConversationState.IDLE
        userEnergy = null
        userSleep = null
        userActivity = null
        userPreference = null
    }
}