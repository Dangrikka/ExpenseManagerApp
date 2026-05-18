package com.example.expensemanager.domain.model

import com.google.gson.annotations.SerializedName

data class AiRequest(
    @SerializedName("model")
    val model: String = "llama-3.3-70b-versatile", // Model mới và chuẩn nhất

    @SerializedName("messages")
    val messages: List<AiMessage>
)

data class AiMessage(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
)

data class AiResponse(
    @SerializedName("choices")
    val choices: List<AiChoice>
)

data class AiChoice(
    @SerializedName("message")
    val message: AiMessage
)