// UI con Jetpack Compose

package com.example.competenciaia.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.competenciaia.domain.model.Message
import com.example.competenciaia.presentation.ui.theme.LightPurple
import com.example.competenciaia.presentation.ui.theme.MintGreen
import com.example.competenciaia.presentation.ui.theme.SoftPink

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            reverseLayout = true
        ) {
            items(state.messages.reversed()) { message ->
                MessageBubble(message)
            }
        }

        // --- SECCIÓN DE BOTONES DE RESPUESTA RÁPIDA (NUEVO) ---
        if (state.availableOptions.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.availableOptions.forEach { option ->
                    Button(onClick = {
                        viewModel.processIntent(ChatIntent.OptionSelected(option))
                    }) {
                        Text(text = option)
                    }
                }
            }
        }
        // -----------------------------------------------------------

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // --- SECCIÓN DE ENTRADA DE TEXTO ---
        // Se oculta si hay opciones disponibles para guiar al usuario
        if (state.availableOptions.isEmpty()) {
            MessageInput(onSendMessage = { messageText ->
                viewModel.processIntent(ChatIntent.SendMessage(messageText))
            })
        }
    }
}

@Composable
fun MessageInput(onSendMessage: (String) -> Unit) {
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = textState,
            onValueChange = { textState = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribe un mensaje...") },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                if (textState.text.isNotBlank()) {
                    onSendMessage(textState.text)
                    textState = TextFieldValue("")
                }
            },
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Enviar")
        }
    }
}


@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 24.dp,
                        topEnd = 24.dp,
                        bottomStart = if (message.isFromUser) 24.dp else 0.dp,
                        bottomEnd = if (message.isFromUser) 0.dp else 24.dp
                    )
                )
                .background(if (message.isFromUser) MintGreen else SoftPink)
                .padding(16.dp)
        ) {
            Text(text = message.text, color = Color.Black)
        }
    }
}