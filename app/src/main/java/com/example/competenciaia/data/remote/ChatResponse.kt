package com.example.competenciaia.data.remote

data class ChatResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: MessageDto
)