package com.example.expensemanager.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.domain.model.StatsModel
import com.example.expensemanager.domain.usecase.GetStatsUseCase
import com.example.expensemanager.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val getStatsUseCase: GetStatsUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _stats = MutableStateFlow<Resource<StatsModel>>(Resource.Loading())
    val stats: StateFlow<Resource<StatsModel>> = _stats

    // Trạng thái lưu Tháng và Năm hiện tại
    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1)
    val currentMonth: StateFlow<Int> = _currentMonth

    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear

    init {
        loadStats()
    }

    // Hàm tiến/lùi tháng
    fun changeMonth(isNext: Boolean) {
        var newMonth = _currentMonth.value
        var newYear = _currentYear.value

        if (isNext) {
            if (newMonth == 12) {
                newMonth = 1
                newYear++
            } else {
                newMonth++
            }
        } else {
            if (newMonth == 1) {
                newMonth = 12
                newYear--
            } else {
                newMonth--
            }
        }

        _currentMonth.value = newMonth
        _currentYear.value = newYear
        loadStats() // Tải lại dữ liệu khi đổi tháng
    }

    private fun loadStats() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _stats.value = Resource.Error("Chưa đăng nhập")
                return@launch
            }

            val currentUserId = user.uid
            _stats.value = Resource.Loading()

            val m = _currentMonth.value
            val y = _currentYear.value

            getStatsUseCase(currentUserId, m, y)
                .catch { e ->
                    _stats.value = Resource.Error(e.message ?: "Lỗi tải thống kê")
                }
                .collect { statsModel ->
                    _stats.value = Resource.Success(statsModel)
                }
        }
    }
}