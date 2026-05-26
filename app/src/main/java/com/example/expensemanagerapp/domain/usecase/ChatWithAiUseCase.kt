package com.example.expensemanager.domain.usecase

import android.util.Log
import com.example.expensemanager.data.remote.GroqApiService
import com.example.expensemanager.domain.model.AiMessage
import com.example.expensemanager.domain.model.AiRequest
import com.example.expensemanager.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import javax.inject.Inject
import com.example.expensemanager.domain.model.TransactionModel

class ChatWithAiUseCase @Inject constructor(
    private val apiService: GroqApiService
) {
    suspend operator fun invoke(
        messages: List<AiMessage>,
        currentBalance: String,
        transactions: List<TransactionModel>
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                // Lấy 15 giao dịch gần nhất
                val recentTx = transactions.sortedByDescending { it.date }.take(15)
                val txString = recentTx.joinToString("\n") { 
                    "- ${it.date}: ${it.title} (${it.category}) -> ${it.amount} (${it.type})"
                }
                
                // Thiết lập System Prompt cung cấp bối cảnh tài chính
                val systemPrompt = """
                    Bạn là trợ lý tài chính cá nhân tên là AI.
                    Nhiệm vụ: Tư vấn ngắn gọn, thiết thực, thân thiện, xưng hô "Mình - Bạn". Không dùng các ký hiệu markdown như *, #. Tối đa 4 câu.
                    
                    THÔNG TIN TÀI CHÍNH CỦA KHÁCH HÀNG:
                    - Số dư hiện tại: $currentBalance
                    - Các giao dịch gần đây:
                    $txString
                    
                    Dựa vào thông tin trên và lịch sử trò chuyện, hãy trả lời câu hỏi của khách hàng.
                """.trimIndent()

                val apiMessages = mutableListOf<AiMessage>()
                apiMessages.add(AiMessage(role = "system", content = systemPrompt))
                apiMessages.addAll(messages)

                val request = AiRequest(
                    model = "llama-3.1-8b-instant",
                    messages = apiMessages
                )

                val response = apiService.getChatCompletion("Bearer ${Constants.GROQ_API_KEY}", request)

                response.choices.firstOrNull()?.message?.content
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("ChatWithAiUseCase", "Lỗi HTTP ${e.code()}: $errorBody")
                "Lỗi mạng (HTTP ${e.code()}): $errorBody"
            } catch (e: Exception) {
                Log.e("ChatWithAiUseCase", "Lỗi hệ thống: ${e.message}")
                "Lỗi cục bộ: ${e.message}"
            }
        }
    }
}