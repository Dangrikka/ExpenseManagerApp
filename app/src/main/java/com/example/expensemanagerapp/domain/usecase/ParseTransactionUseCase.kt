package com.example.expensemanager.domain.usecase

import com.example.expensemanager.data.remote.GroqApiService
import com.example.expensemanager.domain.model.AiMessage
import com.example.expensemanager.domain.model.AiRequest
import com.example.expensemanager.utils.Constants
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
class ParseTransactionUseCase @Inject constructor(
    private val apiService: GroqApiService
) {
    suspend operator fun invoke(input: String): Map<String, Any>? {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Bạn là một máy bóc tách dữ liệu tài chính. Hãy trích xuất thông tin từ câu sau: "$input"
                    Trả về DUY NHẤT một đối tượng JSON với các trường:
                    - "title": Tên giao dịch (viết hoa chữ cái đầu)
                    - "amount": Số tiền (kiểu Long)
                    - "category": Chọn 1 trong các loại: Ăn uống, Di chuyển, Mua sắm, Lương, Khác
                    - "type": "EXPENSE" nếu là chi tiêu, "INCOME" nếu là thu nhập.
                    
                    Ví dụ: "Ăn phở 50k" -> {"title": "Ăn phở", "amount": 50000, "category": "Ăn uống", "type": "EXPENSE"}
                    Lưu ý: "k" hoặc "ngàn" tương ứng với 1000. Chỉ trả về JSON, không giải thích gì thêm.
                """.trimIndent()
                val request = AiRequest(
                    messages = listOf(AiMessage(role = "user", content = prompt))
                )
                val response = apiService.getFinancialAdvice("Bearer ${Constants.GROQ_API_KEY}", request = request)
                val jsonString = response.choices.firstOrNull()?.message?.content ?: return@withContext null
                val mapType = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
                Gson().fromJson<Map<String, Any>>(jsonString, mapType)
            } catch (e: Exception) {
                null
            }
        }
    }
}