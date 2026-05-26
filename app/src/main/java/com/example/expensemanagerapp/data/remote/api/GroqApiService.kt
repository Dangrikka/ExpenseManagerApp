package com.example.expensemanager.data.remote

import com.example.expensemanager.domain.model.AiRequest
import com.example.expensemanager.domain.model.AiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("openai/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") token: String,
        @Body request: AiRequest
    ): AiResponse
    @POST("openai/v1/chat/completions")
    suspend fun getFinancialAdvice(
        @Header("Authorization") authHeader: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: AiRequest
    ): AiResponse
}

