package com.example.expensemanager.domain.usecase

import com.example.expensemanager.data.remote.GroqApiService
import com.example.expensemanager.domain.model.AiMessage
import com.example.expensemanager.domain.model.AiRequest
import com.example.expensemanager.domain.model.StatsModel
import com.example.expensemanager.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFinancialAdviceUseCase @Inject constructor(
    private val apiService: GroqApiService // Inject Retrofit Service vào đây
) {
    suspend operator fun invoke(stats: StatsModel): String {
        return withContext(Dispatchers.IO) {
            try {
                // Tạo câu hỏi (Prompt)
                val prompt = """
                    Bạn là một chuyên gia tư vấn tài chính cá nhân.
                    Dưới đây là thống kê chi tiêu tháng này của tôi:
                    - Tổng thu nhập: ${stats.totalIncome} VND
                    - Tổng chi tiêu: ${stats.totalExpense} VND
                    - Chi tiết theo danh mục: ${stats.expenseByCategory}

                    Hãy phân tích ngắn gọn tình hình tài chính của tôi. 
                    Viết ra 2-3 câu nhận xét đánh giá, và đưa ra 1 lời khuyên thực tế nhất để tiết kiệm. 
                    Sử dụng giọng văn thân thiện, xưng "bạn". Không dùng định dạng markdown hoa lá, chỉ dùng văn bản thuần túy.
                """.trimIndent()

                // Gói câu hỏi vào cái "khuôn" Request
                val request = AiRequest(
                    messages = listOf(
                        AiMessage(role = "user", content = prompt)
                    )
                )

                // Gọi lên server Groq
                val response = apiService.getFinancialAdvice(
                    authHeader = "Bearer ${Constants.GROQ_API_KEY}",
                    request = request
                )

                // Lọc lấy câu trả lời từ JSON trả về
                val advice = response.choices.firstOrNull()?.message?.content

                advice ?: "Groq AI đang bận suy nghĩ. Bạn thử lại sau nhé!"

            } catch (e: Exception) {
                e.printStackTrace()
                "Xin lỗi, không thể kết nối với Cố vấn AI lúc này. Lỗi: ${e.message}"
            }
        }
    }
}