package com.example.expensemanager.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.domain.usecase.GetFinancialAdviceUseCase
import com.example.expensemanager.domain.usecase.GetStatsUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getStatsUseCase: GetStatsUseCase,
    private val getFinancialAdviceUseCase: GetFinancialAdviceUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Trạng thái lưu trữ câu trả lời của AI
    private val _aiAdvice = MutableStateFlow("Nhấn 'Phân tích' để AI đánh giá báo cáo chi tiêu của bạn nhé!")
    val aiAdvice: StateFlow<String> = _aiAdvice

    // Trạng thái vòng xoay loading
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    fun getAdviceFromAI(month: Int, year: Int) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _aiAdvice.value = "Vui lòng đăng nhập để Cố vấn AI có thể phân tích dữ liệu của bạn."
            return
        }

        viewModelScope.launch {
            _isAiLoading.value = true
            _aiAdvice.value = "" // Xóa text cũ trong lúc chờ

            try {
                // 1. Lấy dữ liệu thống kê của tháng đang chọn (Dùng .first() để chỉ lấy 1 lần duy nhất)
                val currentStats = getStatsUseCase(userId, month, year).first()

                // 2. Gửi dữ liệu đó lên máy chủ Groq AI để xin lời khuyên
                val advice = getFinancialAdviceUseCase(currentStats)

                // 3. Nhận kết quả và hiển thị
                _aiAdvice.value = advice
            } catch (e: Exception) {
                e.printStackTrace()
                _aiAdvice.value = "Đã xảy ra sự cố khi gọi AI: ${e.message}"
            } finally {
                // Tắt vòng xoay loading dù thành công hay thất bại
                _isAiLoading.value = false
            }
        }
    }
}